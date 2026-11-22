package me.konsolas.aac.detectors.movement;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import me.konsolas.aac.AAC;
import me.konsolas.aac.api.HackType;
import me.konsolas.aac.detectors.Detector;
import me.konsolas.aac.detectors.MoveListener;
import me.konsolas.aac.util.blocks.RelativeBlockCache;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;

public final class Jesus extends Detector implements MoveListener {
   private final Map<UUID, Location> safeLocations = new HashMap<>();
   private Map<UUID, Integer> waterTicks = new HashMap<>();
   private Set<UUID> waterWalkCandidates = new HashSet<>();
   private boolean teleport = true;

   public Jesus(AAC plugin) {
      super(plugin);
      this.teleport = this.plugin.getConfig().getBoolean("jesus.teleport");
      this.scheduledTaskIds.add(Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, new DecayViolationsTask(this), 20L, 100L));
   }

   public static boolean isInOrAboveLiquid(Player player) {
      RelativeBlockCache relativeBlockCache = new RelativeBlockCache(player.getLocation());
      return isSolidNonLiquid(relativeBlockCache.getSubtractedBlock(0.0, 0.5, 0.3))
            || isSolidNonLiquid(relativeBlockCache.getSubtractedBlock(0.3, 0.6, 0.0))
            || isSolidNonLiquid(relativeBlockCache.getSubtractedBlock(0.0, 0.6, -0.3))
            || isSolidNonLiquid(relativeBlockCache.getSubtractedBlock(-0.3, 0.6, 0.0))
            || isSolidNonLiquid(relativeBlockCache.getSubtractedBlock(-0.3, 0.6, -0.3))
            || isSolidNonLiquid(relativeBlockCache.getSubtractedBlock(-0.3, 0.6, 0.3))
            || isSolidNonLiquid(relativeBlockCache.getSubtractedBlock(0.3, 0.6, -0.3))
            || isSolidNonLiquid(relativeBlockCache.getSubtractedBlock(0.3, 0.6, 0.3))
            || isSolidNonLiquid(relativeBlockCache.getSubtractedBlock(0.0, 0.0, 0.3))
            || isSolidNonLiquid(relativeBlockCache.getSubtractedBlock(0.3, 0.0, 0.0))
            || isSolidNonLiquid(relativeBlockCache.getSubtractedBlock(0.0, 0.0, -0.3))
            || isSolidNonLiquid(relativeBlockCache.getSubtractedBlock(-0.3, 0.0, 0.0))
            || isSolidNonLiquid(relativeBlockCache.getSubtractedBlock(-0.3, 0.0, -0.3))
            || isSolidNonLiquid(relativeBlockCache.getSubtractedBlock(-0.3, 0.0, 0.3))
            || isSolidNonLiquid(relativeBlockCache.getSubtractedBlock(0.3, 0.0, -0.3))
         ? false
         : relativeBlockCache.getSubtractedBlock(0.0, 0.5, 0.0).isLiquid() || relativeBlockCache.getSubtractedBlock(0.0, 1.5, 0.0).isLiquid() || relativeBlockCache.getCenterBlock().isLiquid();
   }

   private static boolean isSolidNonLiquid(Block block) {
      return !block.isLiquid() && !block.isEmpty();
   }

   @Override
   public boolean shouldProcessPlayerMove(PlayerMoveEvent event) {
      if (!event.getPlayer().isFlying() && event.getPlayer().getVehicle() == null) {
         for (Entity entity : event.getPlayer().getNearbyEntities(3.0, 3.0, 3.0)) {
            if (entity instanceof Boat) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   @Override
   public void onPlayerMove(PlayerMoveEvent event, boolean flag, boolean flag2, RelativeBlockCache relativeBlockCache, RelativeBlockCache relativeBlockCache2) {
      if (isInOrAboveLiquid(event.getPlayer())
         && event.getPlayer().getLocation().add(0.0, 0.5, 0.0).getBlock().isEmpty()
         && (
            event.getPlayer().getLocation().subtract(0.0, 1.5, 0.0).getBlock().isLiquid()
               || event.getPlayer().getLocation().subtract(0.0, 0.5, 0.0).getBlock().isLiquid()
         )
         && !this.plugin.combatLogoutListener.hasRecentDamage(event.getPlayer())) {
         if (event.getPlayer().getLocation().subtract(0.0, 0.2, 0.0).getBlock().isLiquid()) {
            if (!this.waterTicks.containsKey(event.getPlayer().getUniqueId())) {
               this.waterTicks.put(event.getPlayer().getUniqueId(), 0);
            }

            this.waterWalkCandidates.add(event.getPlayer().getUniqueId());
         }

         if (this.waterWalkCandidates.contains(event.getPlayer().getUniqueId())) {
            if (!this.waterTicks.containsKey(event.getPlayer().getUniqueId())) {
               this.waterTicks.put(event.getPlayer().getUniqueId(), 0);
            }

            this.waterTicks.put(event.getPlayer().getUniqueId(), this.waterTicks.get(event.getPlayer().getUniqueId()) + 1);
            if (this.waterTicks.get(event.getPlayer().getUniqueId()) > 20
               && this.recordViolation(
                  event.getPlayer().getUniqueId(), 1, HackType.JESUS, event.getPlayer().getName() + " failed Jesus: tried to float above water"
               )
               && this.teleport) {
               event.setTo(event.getFrom().subtract(0.0, 0.1, 0.0));
            }
         }
      } else {
         this.waterTicks.remove(event.getPlayer().getUniqueId());
      }
   }

   private double getLiquidDepth(Location location, int i) {
      if (this.withY(location, location.getY() + 1.5).getBlock().isLiquid()) {
         return 256.0;
      }

      for (double y = location.getY() + 1.0; y > location.getY() - i; y--) {
         if (this.withY(location, y).getBlock().isLiquid()) {
            return this.withY(location, y).getBlock().getY() + 1;
         }
      }

      return 256.0;
   }

   private Location withY(Location location, double d) {
      location.setY(d);
      return location;
   }

   static Map getSafeLocations(Jesus jesus) {
      return jesus.safeLocations;
   }


   private static class DecayViolationsTask implements Runnable {
      final Jesus jesus;

      DecayViolationsTask(Jesus jesus) {
         this.jesus = jesus;
      }

      @Override
      public void run() {
         for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            Jesus.getSafeLocations(this.jesus).put(player.getUniqueId(), player.getLocation());
            this.jesus.decreaseViolationLevel(player.getUniqueId(), 1);
         }
      }
   }
}
