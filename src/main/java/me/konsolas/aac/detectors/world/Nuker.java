package me.konsolas.aac.detectors.world;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import me.konsolas.aac.AAC;
import me.konsolas.aac.api.HackType;
import me.konsolas.aac.detectors.Detector;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.util.Vector;

public final class Nuker extends Detector {
   private int maxBreaksPerSecond = 40;
   private int vlDivisor = 41;
   private Map<UUID, Integer> breakCounts = new HashMap<>();
   private Map<UUID, Vector> lastBrokenPositions = new HashMap<>();

   public Nuker(AAC plugin) {
      super(plugin);
      this.maxBreaksPerSecond = this.plugin.getConfig().getInt("nuker.max_breaks_per_second", this.maxBreaksPerSecond);
      this.vlDivisor = this.plugin.getConfig().getInt("nuker.vl_divisor", this.vlDivisor);
      this.scheduledTaskIds.add(Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, new ResetBreakCountTask(this), 6L, 20L));
      this.scheduledTaskIds.add(Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, new DecayViolationsTask(this), 6L, 50L));
   }

   @EventHandler
   public void onBlockBreak(BlockBreakEvent event) {
      if (!this.plugin.checkManager.isPlayerExempt(event.getPlayer())) {
         if (!this.lastBrokenPositions.containsKey(event.getPlayer().getUniqueId())
            || !this.lastBrokenPositions.get(event.getPlayer().getUniqueId()).equals(event.getBlock().getLocation().toVector())) {
            if (!this.breakCounts.containsKey(event.getPlayer().getUniqueId())) {
               this.breakCounts.put(event.getPlayer().getUniqueId(), 1);
            } else {
               this.breakCounts.put(event.getPlayer().getUniqueId(), this.breakCounts.get(event.getPlayer().getUniqueId()) + 1);
               if (this.breakCounts.get(event.getPlayer().getUniqueId()) > this.maxBreaksPerSecond) {
                  if (this.breakCounts.get(event.getPlayer().getUniqueId()) % this.vlDivisor == 0
                     && this.recordViolation(
                        event.getPlayer().getUniqueId(),
                        1,
                        HackType.NUKER,
                        event.getPlayer().getName() + " failed Nuker: broke too many blocks (" + this.breakCounts.get(event.getPlayer().getUniqueId()) + " blocks/second)"
                     )) {
                     event.setCancelled(true);
                  }

                  return;
               }
            }

            this.lastBrokenPositions.put(event.getPlayer().getUniqueId(), event.getBlock().getLocation().toVector());
         }
      }
   }

   static Map getBreakCounts(Nuker nuker) {
      return nuker.breakCounts;
   }


   private static class ResetBreakCountTask implements Runnable {
      final Nuker nuker;

      ResetBreakCountTask(Nuker nuker) {
         this.nuker = nuker;
      }

      @Override
      public void run() {
         for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            Nuker.getBreakCounts(this.nuker).put(player.getUniqueId(), 0);
         }
      }
   }

   private static class DecayViolationsTask implements Runnable {
      final Nuker nuker;

      DecayViolationsTask(Nuker nuker) {
         this.nuker = nuker;
      }

      @Override
      public void run() {
         for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            this.nuker.decreaseViolationLevel(player.getUniqueId(), 1);
         }
      }
   }
}
