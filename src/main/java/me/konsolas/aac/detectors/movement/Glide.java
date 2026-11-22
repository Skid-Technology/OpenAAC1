package me.konsolas.aac.detectors.movement;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import me.konsolas.aac.AAC;
import me.konsolas.aac.api.HackType;
import me.konsolas.aac.detectors.Detector;
import me.konsolas.aac.detectors.MoveListener;
import me.konsolas.aac.util.blocks.BlockCollisionChecks;
import me.konsolas.aac.util.blocks.MovementObstacleChecks;
import me.konsolas.aac.util.blocks.RelativeBlockCache;
import me.konsolas.aac.util.movement.VerticalMotionModel;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public final class Glide extends Detector implements MoveListener {
   private double tolerance = 0.1;
   private boolean teleport = true;
   private double specialTolerance = 0.8;
   private int minVl = 5;
   private int velTicks = 12;
   private Set<UUID> exemptPlayers = new HashSet<>();
   private Map<UUID, Long> lastMoveTicks = new HashMap<>();
   private Map<UUID, Double> fallDistances = new HashMap<>();
   private Map<UUID, Double> lastDeltaY = new HashMap<>();
   private Map<UUID, Integer> violationStreaks = new HashMap<>();
   private Set<UUID> flightToggleBypass = new HashSet<>();

   public Glide(AAC plugin) {
      super(plugin);
      this.tolerance = this.plugin.getConfig().getDouble("glide.tolerance", this.tolerance);
      this.teleport = this.plugin.getConfig().getBoolean("glide.teleport", this.teleport);
      this.specialTolerance = this.plugin.getConfig().getDouble("glide.special_tolerance", this.specialTolerance);
      this.minVl = this.plugin.getConfig().getInt("glide.min_vl", this.minVl);
      this.velTicks = this.plugin.getConfig().getInt("glide.vel_ticks", this.velTicks);
      this.scheduledTaskIds.add(Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, new DecayViolationsTask(this), 32L, 23L));
   }

   @Override
   public boolean shouldProcessPlayerMove(PlayerMoveEvent event) {
      this.recordMoveTick(event.getPlayer());
      return event.getPlayer().getGameMode() != GameMode.SPECTATOR
         && !event.getPlayer().isFlying()
         && !event.getPlayer().isDead()
         && event.getPlayer().getLocation().getY() > -70.0
         && !event.getPlayer().getAllowFlight();
   }

   private void recordMoveTick(Player player) {
      if (!player.isOnline()) {
         this.lastMoveTicks.remove(player.getUniqueId());
      } else {
         this.lastMoveTicks.put(player.getUniqueId(), this.currentTick);
      }
   }

   @Override
   public void setCurrentTick(long ticks) {
      super.setCurrentTick(ticks);
      if (ticks % 29L == 0L) {
         this.exemptPlayers.clear();
      }

      if (ticks % 3L == 0L) {
         Iterator iterator = this.lastMoveTicks.entrySet().iterator();

         while (iterator.hasNext()) {
            Entry entry = (Entry)iterator.next();
            if (ticks - (Long)entry.getValue() > 5L) {
               UUID uuid = (UUID)entry.getKey();
               Player player = Bukkit.getPlayer(uuid);
               if (player == null || !player.isOnline()) {
                  iterator.remove();
               } else if (!this.plugin.checkManager.isPlayerExempt(player)) {
                  if (!BlockCollisionChecks.isOnGround(player.getLocation())) {
                     PlayerMoveEvent event = new PlayerMoveEvent(player, player.getLocation(), player.getLocation());
                     if (this.shouldProcessPlayerMove(event)) {
                        this.onPlayerMove(event, false, false, new RelativeBlockCache(event.getFrom()), new RelativeBlockCache(event.getTo()));
                     }
                  }
               } else {
                  iterator.remove();
               }
            }
         }
      }
   }

   @EventHandler
   public void onGlideToggleFlight(PlayerToggleFlightEvent event) {
      this.fallDistances.remove(event.getPlayer().getUniqueId());
      this.flightToggleBypass.add(event.getPlayer().getUniqueId());
   }

   @Override
   public void onPlayerMove(PlayerMoveEvent event, boolean flag, boolean flag2, RelativeBlockCache relativeBlockCache, RelativeBlockCache relativeBlockCache2) {
      double y = event.getTo().getY() - event.getFrom().getY();
      double d = this.tolerance;
      long now = this.plugin.playerState.getLastVelocityPacketTime(event.getPlayer()) != Long.MIN_VALUE
         ? System.currentTimeMillis() - this.plugin.playerState.getLastVelocityPacketTime(event.getPlayer())
         : Long.MAX_VALUE;
      boolean flag3 = this.plugin.playerState.hasActiveVelocity(event.getPlayer()) || now < this.velTicks * 50;
      int value = 0;

      for (PotionEffect potionEffect : event.getPlayer().getActivePotionEffects()) {
         if (potionEffect.getType().equals(PotionEffectType.JUMP)) {
            value = potionEffect.getAmplifier() + 1;
         }
      }

      boolean flag4 = this.flightToggleBypass.contains(event.getPlayer().getUniqueId());
      this.flightToggleBypass.remove(event.getPlayer().getUniqueId());
      if (y == 0.0 && flag) {
         this.flightToggleBypass.add(event.getPlayer().getUniqueId());
      }

      if (!flag && flag4) {
         flag = true;
      }

      if (!this.fallDistances.containsKey(event.getPlayer().getUniqueId())) {
         this.fallDistances.put(event.getPlayer().getUniqueId(), y);
      } else {
         if (!flag2 && VerticalMotionModel.predictNextDeltaY(this.fallDistances.get(event.getPlayer().getUniqueId())) < y - d / 2.0 && y < 0.0 && BlockCollisionChecks.isNearLava(relativeBlockCache2)) {
            flag2 = true;
            this.flightToggleBypass.add(event.getPlayer().getUniqueId());
         }

         boolean flag5 = VerticalMotionModel.isValidVerticalMotion(
            y,
            flag,
            flag2,
            flag3,
            this.velTicks,
            now,
            this.fallDistances.get(event.getPlayer().getUniqueId()),
            d,
            value,
            this.plugin.playerState.getLastHorizontalVelocity(event.getPlayer())
         );
         boolean flag6 = shouldCheckGlide(event, flag, flag2);
         if (flag6 && !isNearWebOrPiston(relativeBlockCache)) {
            boolean flag7 = event.getFrom().getBlock().getType() == Material.SLIME_BLOCK
               || event.getFrom().clone().subtract(0.0, 1.0, 0.0).getBlock().getType() == Material.SLIME_BLOCK
               || event.getFrom().clone().subtract(0.0, 2.0, 0.0).getBlock().getType() == Material.SLIME_BLOCK;
            boolean flag8 = event.getTo().getBlock().getType() == Material.SLIME_BLOCK
               || event.getTo().clone().subtract(0.0, 1.0, 0.0).getBlock().getType() == Material.SLIME_BLOCK
               || event.getFrom().clone().subtract(0.0, 2.0, 0.0).getBlock().getType() == Material.SLIME_BLOCK;
            if (flag7 || flag8) {
               flag5 = VerticalMotionModel.isWithinUpperBound(y, 1.0, d);
               if (!this.lastDeltaY.containsKey(event.getPlayer().getUniqueId())) {
                  this.lastDeltaY.put(event.getPlayer().getUniqueId(), Math.abs(y) < 1.0 ? 1.0 : Math.abs(y));
               } else {
                  double d2 = this.lastDeltaY.get(event.getPlayer().getUniqueId());
                  if (Math.abs(y) > d2) {
                     this.lastDeltaY.put(event.getPlayer().getUniqueId(), Math.abs(y));
                  }
               }
            }

            if (this.lastDeltaY.containsKey(event.getPlayer().getUniqueId())) {
               if (this.violationStreaks.containsKey(event.getPlayer().getUniqueId())) {
                  double d3 = VerticalMotionModel.scaleVerticalEstimate(this.lastDeltaY.get(event.getPlayer().getUniqueId()));
                  flag5 = VerticalMotionModel.isWithinUpperBound(y, d3, d);
                  if (this.violationStreaks.get(event.getPlayer().getUniqueId()) >= 5) {
                     this.lastDeltaY.remove(event.getPlayer().getUniqueId());
                     this.violationStreaks.remove(event.getPlayer().getUniqueId());
                  }
               } else {
                  this.violationStreaks.put(event.getPlayer().getUniqueId(), 0);
                  double d4 = VerticalMotionModel.scaleVerticalEstimate(this.lastDeltaY.get(event.getPlayer().getUniqueId()));
                  flag5 = VerticalMotionModel.isWithinUpperBound(y, d4, d);
               }

               if (this.violationStreaks.containsKey(event.getPlayer().getUniqueId())) {
                  this.violationStreaks.put(event.getPlayer().getUniqueId(), this.violationStreaks.get(event.getPlayer().getUniqueId()) + 1);
               }
            }

            if (!flag7 && flag8 && !flag && flag2) {
               this.lastDeltaY
                  .put(
                     event.getPlayer().getUniqueId(),
                     VerticalMotionModel.predictNextDeltaY(this.fallDistances.get(event.getPlayer().getUniqueId())) < 0.9
                        ? 0.9
                        : VerticalMotionModel.predictNextDeltaY(this.fallDistances.get(event.getPlayer().getUniqueId()))
                  );
            }

            if (this.fallDistances.get(event.getPlayer().getUniqueId()) > 0.0 && y < 0.0) {
               flag5 = true;
            }

            if (flag && flag2 && y > 0.0) {
               flag5 = true;
            }
         } else {
            flag5 = true;
         }

         if (flag2 && Math.abs(y) < 0.8) {
            flag5 = true;
         }

         if (!flag5 && !this.exemptPlayers.contains(event.getPlayer().getUniqueId()) && (y < 0.0 && y > 2.0 || flag3)) {
            this.exemptPlayers.add(event.getPlayer().getUniqueId());
            flag5 = true;
         }

         if (this.fallDistances.get(event.getPlayer().getUniqueId()) >= 0.0 && y < 0.0 && y > -0.6) {
            flag5 = true;
         }

         if (!flag5
            && this.recordViolation(
               event.getPlayer().getUniqueId(),
               2,
               HackType.GLIDE,
               event.getPlayer().getName()
                  + " failed Glide: didn't fall correctly (change in y:"
                  + y
                  + ", expected:"
                  + VerticalMotionModel.predictNextDeltaY(this.fallDistances.get(event.getPlayer().getUniqueId()))
                  + ")"
            )
            && this.teleport
            && (this.getViolationLevel(event.getPlayer()) > this.minVl || Math.abs(y) > 1.5)) {
            applySetback(event.getPlayer(), event, this, this.plugin);
            this.fallDistances.put(event.getPlayer().getUniqueId(), event.getPlayer().getVelocity().getY());
         }

         if (flag5) {
            this.fallDistances.put(event.getPlayer().getUniqueId(), y);
         }
      }
   }

   public static boolean shouldCheckGlide(PlayerMoveEvent event, boolean flag, boolean flag2) {
      if (event.getPlayer().getVehicle() != null) {
         return false;
      }

      if (BlockCollisionChecks.isOnClimbable(event.getPlayer())) {
         return false;
      }

      RelativeBlockCache relativeBlockCache = new RelativeBlockCache(event.getTo());
      if (!relativeBlockCache.getRelativeBlock(0.0, 3.0, 0.0).getType().isSolid() && !relativeBlockCache.getRelativeBlock(0.0, 2.0, 0.0).getType().isSolid()
         || !relativeBlockCache.getSubtractedBlock(0.0, 1.0, 0.0).getType().isSolid() && !relativeBlockCache.getSubtractedBlock(0.0, 2.0, 0.0).getType().isSolid()) {
         for (Entity entity : event.getPlayer().getNearbyEntities(3.0, 3.0, 3.0)) {
            if (entity instanceof Boat) {
               return false;
            }
         }

         return !Speed.isNearLowBlock(event.getPlayer()) && !Jesus.isInOrAboveLiquid(event.getPlayer()) && !isInLiquid(event.getPlayer());
      } else {
         return false;
      }
   }

   public static boolean isInLiquid(Player player) {
      RelativeBlockCache relativeBlockCache = new RelativeBlockCache(player.getLocation());
      return relativeBlockCache.getSubtractedBlock(0.0, 0.5, 0.45).isLiquid()
         || relativeBlockCache.getSubtractedBlock(0.45, 0.6, 0.0).isLiquid()
         || relativeBlockCache.getSubtractedBlock(0.0, 0.6, -0.45).isLiquid()
         || relativeBlockCache.getSubtractedBlock(-0.45, 0.6, 0.0).isLiquid()
         || relativeBlockCache.getSubtractedBlock(-0.45, 0.6, -0.45).isLiquid()
         || relativeBlockCache.getSubtractedBlock(-0.45, 0.6, 0.45).isLiquid()
         || relativeBlockCache.getSubtractedBlock(0.45, 0.6, -0.45).isLiquid()
         || relativeBlockCache.getSubtractedBlock(0.45, 0.6, 0.45).isLiquid();
   }

   public static boolean isNearWebOrPiston(RelativeBlockCache relativeBlockCache) {
      HashSet<Block> set = new HashSet<>();
      set.add(relativeBlockCache.getSubtractedBlock(0.0, 0.1, 0.3));
      set.add(relativeBlockCache.getSubtractedBlock(0.3, 0.1, 0.0));
      set.add(relativeBlockCache.getSubtractedBlock(0.0, 0.1, -0.3));
      set.add(relativeBlockCache.getSubtractedBlock(-0.3, 0.1, 0.0));
      set.add(relativeBlockCache.getSubtractedBlock(-0.3, 0.1, -0.3));
      set.add(relativeBlockCache.getSubtractedBlock(-0.3, 0.1, 0.3));
      set.add(relativeBlockCache.getSubtractedBlock(0.3, 0.1, -0.3));
      set.add(relativeBlockCache.getSubtractedBlock(0.3, 0.1, 0.3));
      set.add(relativeBlockCache.getSubtractedBlock(0.0, 0.1, 0.0));

      for (Block block : set) {
         Block block2 = block.getRelative(BlockFace.UP);
         Block block3 = block.getRelative(BlockFace.DOWN);
         Block block4 = block.getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN);
         if (isWebOrPiston(block) || isWebOrPiston(block3) || isWebOrPiston(block2) || isWebOrPiston(block4)) {
            return true;
         }

         if (block2.getRelative(BlockFace.UP).getType() == Material.WEB) {
            return true;
         }
      }

      return false;
   }

   private static boolean isWebOrPiston(Block block) {
      return block.getType() == Material.PISTON_BASE
         || block.getType() == Material.PISTON_EXTENSION
         || block.getType() == Material.PISTON_MOVING_PIECE
         || block.getType() == Material.PISTON_STICKY_BASE
         || block.getType() == Material.WEB;
   }

   @EventHandler
   public void onFlightDisabled(PlayerToggleFlightEvent event) {
      this.fallDistances.remove(event.getPlayer().getUniqueId());
   }

   @EventHandler
   public void onBlockBreak(BlockBreakEvent event) {
      this.fallDistances.remove(event.getPlayer().getUniqueId());
   }

   @EventHandler
   public void onPlayerRespawn(PlayerRespawnEvent event) {
      this.fallDistances.remove(event.getPlayer().getUniqueId());
      this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new ClearRespawnBypassTask(this, event), 4L);
   }

   public static void applySetback(Player player, PlayerMoveEvent event, Detector detector, AAC plugin) {
      applySetbackWithVelocity(player, event, plugin, detector, player.getVelocity().getY() - 0.1);
   }

   private static void applySetbackWithVelocity(Player player, PlayerMoveEvent event, AAC plugin, Detector detector, double d) {
      if (d > -0.05) {
         d = -0.05;
      }

      Location location = plugin.movementDispatcher.getSafeLocation(player);
      if (location != null) {
         double x = Math.abs(event.getTo().getX() - location.getX());
         double z = Math.abs(event.getTo().getZ() - location.getZ());
         double d2 = Math.sqrt(Math.pow(x, 2.0) + Math.pow(z, 2.0));
         if (d2 < 3.0 && event.getFrom().getY() > location.getY()) {
            plugin.movementDispatcher.setTeleportExempt(detector, player);
            player.teleport(location, TeleportCause.UNKNOWN);
         }
      }

      Location location2 = event.getFrom();

      for (double d3 = 0.0; d3 > d && MovementObstacleChecks.isPlayerUnobstructed(new RelativeBlockCache(player.getLocation().add(0.0, d3, 0.0))); d3 -= 0.3) {
         location2 = player.getLocation().add(0.0, d3, 0.0);
      }

      Location location3 = event.getFrom();

      for (double d4 = 0.0; d4 > d && MovementObstacleChecks.isPlayerUnobstructed(new RelativeBlockCache(location2.clone().add(0.0, d4, 0.0))); d4 -= 0.1) {
         location3 = location2.clone().add(0.0, d4, 0.0);
      }

      float f = player.getFallDistance();
      plugin.movementDispatcher.setTeleportExempt(detector, player);
      player.teleport(location2, TeleportCause.UNKNOWN);
      plugin.movementDispatcher.setTeleportExempt(detector, player);
      player.setVelocity(new Vector(0.0, d > 0.0 ? 0.0 : d, 0.0));
      Location location4 = location3;
      double d5 = d;
      plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new DeferredFallSetbackTask(player, location4, f, event, plugin, detector, d5));
      plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new ClearSetbackStateTask(plugin, player), 2L);
   }

   static Set getExemptPlayers(Glide glide) {
      return glide.exemptPlayers;
   }


   static Map getFallDistances(Glide glide) {
      return glide.fallDistances;
   }

   private static class DecayViolationsTask implements Runnable {
      final Glide glide;

      DecayViolationsTask(Glide glide) {
         this.glide = glide;
      }

      @Override
      public void run() {
         Glide.getExemptPlayers(this.glide).clear();

         for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            this.glide.decreaseViolationLevel(player.getUniqueId(), 1);
         }
      }
   }

   private static class ClearRespawnBypassTask implements Runnable {
      final PlayerRespawnEvent event;
      final Glide glide;

      ClearRespawnBypassTask(Glide glide, PlayerRespawnEvent event) {
         this.glide = glide;
         this.event = event;
      }

      @Override
      public void run() {
         Glide.getFallDistances(this.glide).remove(this.event.getPlayer().getUniqueId());
      }
   }

   private static final class DeferredFallSetbackTask implements Runnable {
      final Player player;
      final Location safeLocation;
      final float fallDistance;
      final PlayerMoveEvent event;
      final AAC plugin;
      final Detector detector;
      final double velocityY;

      DeferredFallSetbackTask(Player player, Location location, float f, PlayerMoveEvent event, AAC plugin, Detector detector, double d) {
         this.player = player;
         this.safeLocation = location;
         this.fallDistance = f;
         this.event = event;
         this.plugin = plugin;
         this.detector = detector;
         this.velocityY = d;
      }

      @Override
      public void run() {
         this.player.teleport(this.safeLocation, TeleportCause.UNKNOWN);
         this.player.setFallDistance(this.fallDistance + (float)(this.event.getFrom().getY() - this.safeLocation.getY()));
         this.plugin.movementDispatcher.setTeleportExempt(this.detector, this.player);
         this.player.setVelocity(new Vector(0.0, this.velocityY > 0.0 ? 0.0 : this.velocityY, 0.0));
      }
   }

   private static final class ClearSetbackStateTask implements Runnable {
      final AAC plugin;
      final Player player;

      ClearSetbackStateTask(AAC plugin, Player player) {
         this.plugin = plugin;
         this.player = player;
      }

      @Override
      public void run() {
         this.plugin.movementDispatcher.clearTeleportExempt(this.player);
      }
   }
}
