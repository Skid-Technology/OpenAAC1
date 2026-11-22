package me.konsolas.aac.detectors.combat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import me.konsolas.aac.AAC;
import me.konsolas.aac.api.HackType;
import me.konsolas.aac.detectors.Detector;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class Fastbow extends Detector {
   private int shotsFiredThreshold = 2;
   private double minForce = 0.5;
   private Map<UUID, Integer> shotCounts = new HashMap<>();
   private Set<UUID> bowBlockedPlayers = new HashSet<>();

   public Fastbow(AAC plugin) {
      super(plugin);
      this.minForce = this.plugin.getConfig().getDouble("fastbow.min_force", this.minForce);
      this.shotsFiredThreshold = this.plugin.getConfig().getInt("fastbow.shots_fired_threshold", this.shotsFiredThreshold);
      this.scheduledTaskIds.add(Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, new ArrowCountMonitorTask(this), 30L, 20L));
   }

   @EventHandler
   public void onEntityShootBow(EntityShootBowEvent event) {
      if (event.getEntity().getType() == EntityType.PLAYER) {
         if (this.plugin.checkManager.isPlayerExempt((Player)event.getEntity())) {
            return;
         }

         if (this.bowBlockedPlayers.contains(event.getEntity().getUniqueId())) {
            this.plugin
               .verboseLogger
               .logPlayerDebug(
                  (Player)event.getEntity(),
                  "(FastBow) EntityShootBowEvent !allowed force[n:"
                     + this.minForce
                     + ",r:"
                     + event.getForce()
                     + "] + totalShots[t:"
                     + this.shotsFiredThreshold
                     + ",r:"
                     + this.shotCounts.get(event.getEntity().getUniqueId())
               );
            event.setCancelled(true);
            return;
         }

         if (event.getForce() > this.minForce) {
            if (!this.shotCounts.containsKey(event.getEntity().getUniqueId())) {
               this.shotCounts.put(event.getEntity().getUniqueId(), 1);
            } else {
               this.shotCounts.put(event.getEntity().getUniqueId(), this.shotCounts.get(event.getEntity().getUniqueId()) + 1);
            }

            if (this.shotCounts.get(event.getEntity().getUniqueId()) > this.shotsFiredThreshold) {
               event.setCancelled(true);
               this.plugin
                  .verboseLogger
                  .logPlayerDebug(
                     (Player)event.getEntity(),
                     "(FastBow) EntityShootBowEvent !allowed force[n:"
                        + this.minForce
                        + ",r:"
                        + event.getForce()
                        + "] + totalShots[t:"
                        + this.shotsFiredThreshold
                        + ",r:"
                        + this.shotCounts.get(event.getEntity().getUniqueId())
                  );
            } else {
               this.plugin
                  .verboseLogger
                  .logPlayerDebug(
                     (Player)event.getEntity(),
                     "(FastBow) EntityShootBowEvent allowed force[n:"
                        + this.minForce
                        + ",r:"
                        + event.getForce()
                        + "] + totalShots[t:"
                        + this.shotsFiredThreshold
                        + ",r:"
                        + this.shotCounts.get(event.getEntity().getUniqueId())
                  );
            }
         }
      }
   }

   @EventHandler(priority = EventPriority.MONITOR)
   public void onFastbowPlayerJoin(PlayerJoinEvent event) {
      super.onPlayerJoin(event);
      this.shotCounts.put(event.getPlayer().getUniqueId(), 0);
   }

   @EventHandler(priority = EventPriority.MONITOR)
   public void onFastbowPlayerQuit(PlayerQuitEvent event) {
      super.onPlayerQuit(event);
      this.shotCounts.remove(event.getPlayer().getUniqueId());
   }

   static Map<UUID, Integer> getShotCounts(Fastbow fastbow) {
      return fastbow.shotCounts;
   }

   static int getShotsFiredThreshold(Fastbow fastbow) {
      return fastbow.shotsFiredThreshold;
   }

   static Set getBowBlockedPlayers(Fastbow fastbow) {
      return fastbow.bowBlockedPlayers;
   }


   private static class ClearBowBanTask implements Runnable {
      final UUID playerId;
      final ArrowCountMonitorTask monitorTask;

      ClearBowBanTask(ArrowCountMonitorTask arrowCountMonitorTask, UUID uuid) {
         this.monitorTask = arrowCountMonitorTask;
         this.playerId = uuid;
      }

      @Override
      public void run() {
         Fastbow.getBowBlockedPlayers(this.monitorTask.fastbow).remove(this.playerId);

         try {
            Bukkit.getPlayer(this.playerId).sendMessage(AAC.messagePrefix + this.monitorTask.fastbow.getPlugin().messages.getMessage("allow_bow"));
         } catch (NullPointerException ex) {
         }
      }
   }

   private static class ArrowCountMonitorTask implements Runnable {
      final Fastbow fastbow;

      ArrowCountMonitorTask(Fastbow fastbow) {
         this.fastbow = fastbow;
      }

      @Override
      public void run() {
         for (UUID uuid : Fastbow.getShotCounts(this.fastbow).keySet()) {
            if ((Integer)Fastbow.getShotCounts(this.fastbow).get(uuid) > Fastbow.getShotsFiredThreshold(this.fastbow) && !Fastbow.getBowBlockedPlayers(this.fastbow).contains(uuid)) {
               if (this.fastbow.recordViolation(uuid, 4, HackType.FASTBOW, Bukkit.getOfflinePlayer(uuid).getName() + " failed Fastbow: tried to fire " + Fastbow.getShotCounts(this.fastbow).get(uuid) + " arrows in 1 second.")) {
                  Fastbow.getBowBlockedPlayers(this.fastbow).add(uuid);
                  UUID uuid2 = uuid;
                  Bukkit.getPlayer(uuid).sendMessage(AAC.messagePrefix + this.fastbow.getPlugin().messages.getMessage("too_many_shots"));
                  Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.fastbow.getPlugin(), new ClearBowBanTask(this, uuid2), 100L);
               }
            } else if (!Fastbow.getBowBlockedPlayers(this.fastbow).contains(uuid)) {
               this.fastbow.decreaseViolationLevel(uuid, 1);
            }

            Fastbow.getShotCounts(this.fastbow).put(uuid, 0);
         }
      }
   }
}
