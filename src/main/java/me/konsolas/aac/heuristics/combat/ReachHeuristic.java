package me.konsolas.aac.heuristics.combat;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import me.konsolas.aac.AAC;
import me.konsolas.aac.heuristics.base.Heuristic;
import me.konsolas.aac.heuristics.util.SampleData;
import me.konsolas.aac.util.EntityTargetCategories;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ReachHeuristic extends Heuristic {
   private final Map<UUID, PlayerReachHistory> histories = new HashMap<>();
   private int resetTaskId;

   public ReachHeuristic(AAC plugin) {
      super(plugin);

      for (Player player : Bukkit.getServer().getOnlinePlayers()) {
         this.histories.put(player.getUniqueId(), new PlayerReachHistory());
      }

      this.resetTaskId = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new ResetTargetHistoryTask(this), 10L, 10000L);
   }

   @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
   public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
      if (event.getCause() == DamageCause.ENTITY_ATTACK) {
         try {
            if (!EntityTargetCategories.isReachTarget(event.getEntity().getType())) {
               return;
            }

            if (event.getDamager().getType() == EntityType.PLAYER && !this.plugin.checkManager.isPlayerExempt((Player)event.getDamager())) {
               PlayerReachHistory playerReachHistory = this.histories.get(event.getDamager().getUniqueId());
               if (System.currentTimeMillis() - playerReachHistory.lastUpdateTime > 1200L) {
                  playerReachHistory.lastUpdateTime = System.currentTimeMillis();
                  return;
               }

               Player player = (Player)event.getDamager();

               try {
                  if (playerReachHistory.lastTarget != null && playerReachHistory.lastTarget.equals(event.getEntity())) {
                     double d = playerReachHistory.lastLocation.distance(event.getEntity().getLocation());
                     if (d <= 0.1 || player.getLocation().distanceSquared(event.getEntity().getLocation()) <= 1.2) {
                        return;
                     }
                  }
               } catch (Exception ex) {
                  return;
               }

               playerReachHistory.distanceSamples.addSample(player.getLocation().distance(event.getEntity().getLocation()));
               if (playerReachHistory.distanceSamples.getSamples().size() > 12) {
                  double d2 = playerReachHistory.distanceSamples.getMean();
                  this.setHeuristicScore(player, (int)((d2 - 2.85) * 1000.0));
               }

               playerReachHistory.lastUpdateTime = System.currentTimeMillis();
               playerReachHistory.lastLocation = event.getEntity().getLocation();
               playerReachHistory.lastTarget = event.getEntity();
            }
         } catch (NullPointerException ex2) {
            this.plugin.verboseLogger.logViolation("(ReachHeuristic) caught an NPE");
         }
      }
   }

   @Override
   protected void updateScore(Player player) {
   }

   @Override
   public int getDetectionChance(Player player) {
      double d = Math.pow(this.getHeuristicScore(player), 0.9) / 420.0;
      return (int)Math.round(d * 100.0 > 99.0 ? 99.0 : d * 100.0);
   }

   @EventHandler(priority = EventPriority.MONITOR)
   @Override
   public void onPlayerJoin(PlayerJoinEvent event) {
      super.onPlayerJoin(event);
      this.histories.put(event.getPlayer().getUniqueId(), new PlayerReachHistory());
   }

   @EventHandler(priority = EventPriority.MONITOR)
   @Override
   public void onPlayerQuit(PlayerQuitEvent event) {
      super.onPlayerQuit(event);
      this.histories.remove(event.getPlayer().getUniqueId());
   }

   @EventHandler(priority = EventPriority.MONITOR)
   @Override
   public void onPlayerKick(PlayerKickEvent event) {
      super.onPlayerKick(event);
      this.histories.remove(event.getPlayer().getUniqueId());
   }

   static Map getHistories(ReachHeuristic reachHeuristic) {
      return reachHeuristic.histories;
   }

   private static class ResetTargetHistoryTask implements Runnable {
      final ReachHeuristic heuristic;

      ResetTargetHistoryTask(ReachHeuristic reachHeuristic) {
         this.heuristic = reachHeuristic;
      }

      @Override
      public void run() {
         for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            ((PlayerReachHistory)ReachHeuristic.getHistories(this.heuristic).get(player.getUniqueId())).lastTarget = null;
            ((PlayerReachHistory)ReachHeuristic.getHistories(this.heuristic).get(player.getUniqueId())).lastLocation = null;
         }
      }
   }

   private static class PlayerReachHistory {
      public SampleData<Double> distanceSamples = new SampleData<>(8);
      public long lastUpdateTime = System.currentTimeMillis();
      public Location lastLocation = null;
      public Entity lastTarget = null;

      public PlayerReachHistory() {
      }
   }
}
