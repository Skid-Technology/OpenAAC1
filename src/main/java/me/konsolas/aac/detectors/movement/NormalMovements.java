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
import me.konsolas.aac.util.general.NumberUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class NormalMovements extends Detector implements MoveListener {
   private Map<UUID, Integer> stepCounts = new HashMap<>();
   private Map<UUID, Integer> jumpTickCounts = new HashMap<>();
   private Map<UUID, Double> expectedMaxY = new HashMap<>();
   private static final double DELTA_TOLERANCE = 0.02;
   private static final double[] JUMP_VELOCITIES = new double[]{0.42, 0.33, 0.16};
   private static final double STEP_1_HEIGHT = 0.33;
   private static final double STEP_2_HEIGHT = 0.24;
   private static final double STEP_3_HEIGHT = 0.16;
   private static final double GRAVITY_STEP = 0.08;
   private static final double WATER_ASCENT_LIMIT = 0.27;
   private boolean fJump = false;
   private boolean fStep = true;
   private boolean fFloat = true;
   private boolean fDy = true;
   private boolean teleport = true;
   private Set<UUID> teleportExemptPlayers = new HashSet<>();

   public NormalMovements(AAC plugin) {
      super(plugin);
      this.fJump = this.plugin.getConfig().getBoolean("normalmovements.f_jump", this.fJump);
      this.fStep = this.plugin.getConfig().getBoolean("normalmovements.f_step", this.fStep);
      this.fFloat = this.plugin.getConfig().getBoolean("normalmovements.f_float", this.fFloat);
      this.fDy = this.plugin.getConfig().getBoolean("normalmovements.f_dy", this.fDy);
      this.teleport = this.plugin.getConfig().getBoolean("normalmovements.teleport", this.teleport);
      Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, new DecayViolationsTask(this), 1L, 27L);
      this.scheduledTaskIds.add(Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, new ClearBypassTask(this), 22L, 19L));
   }

   @EventHandler
   public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
      this.stepCounts.remove(event.getPlayer().getUniqueId());
      this.jumpTickCounts.remove(event.getPlayer().getUniqueId());
      this.expectedMaxY.remove(event.getPlayer().getUniqueId());
   }

   @Override
   public void onPlayerMove(PlayerMoveEvent event, boolean flag, boolean flag2, RelativeBlockCache relativeBlockCache, RelativeBlockCache relativeBlockCache2) {
      Player player = event.getPlayer();
      int value = 0;

      for (PotionEffect potionEffect : player.getActivePotionEffects()) {
         if (potionEffect.getType().equals(PotionEffectType.JUMP)) {
            value = potionEffect.getAmplifier() + 1;
         }
      }

      double y = event.getTo().getY() - event.getFrom().getY();
      double x = event.getTo().getX() - event.getFrom().getX();
      x = event.getTo().getZ() - event.getFrom().getZ();
      boolean flag3 = isNearPistonOrWeb(player);
      if (this.fJump) {
         if (flag && !flag2 && y > 0.0 && !isNearClimbable(player)) {
            this.stepCounts.put(player.getUniqueId(), 0);
         }

         if (flag2 || Speed.hasSolidCeiling(player) || isNearClimbable(player) || isStandingOnSlime(player) || flag3) {
            this.stepCounts.remove(player.getUniqueId());
         }

         if (this.stepCounts.containsKey(player.getUniqueId())) {
            int value2 = this.stepCounts.get(player.getUniqueId());
            this.stepCounts.put(player.getUniqueId(), value2 + 1);
            if (y <= 0.0
               && value2 < 4
               && this.recordViolation(
                  player.getUniqueId(), 1, HackType.NORMALMOVEMENTS, player.getName() + " failed NormalMovements: moved unexpectedly f:jump:-1 (dY:" + y + ")"
               )) {
               this.handleMovementViolation(event, BadPackets.MovementCategory.FLY);
            }

            switch (value2) {
               case 0:
                  if (NumberUtil.isWithinRange(y, JUMP_VELOCITIES[1], 0.02) || NumberUtil.isWithinRange(y, JUMP_VELOCITIES[2], 0.02)) {
                     this.stepCounts.remove(player.getUniqueId());
                  } else if (NumberUtil.isWithinRange(y, 0.08, 0.02)) {
                     this.stepCounts.put(player.getUniqueId(), 5);
                  } else if (!NumberUtil.isWithinRange(y, value > 0 ? value / 10.0 + JUMP_VELOCITIES[0] : JUMP_VELOCITIES[0], 0.02)
                     && this.recordViolation(
                        player.getUniqueId(),
                        1,
                        HackType.NORMALMOVEMENTS,
                        player.getName() + " failed NormalMovements: moved unexpectedly f:jump:0 (dY:" + y + ")"
                     )) {
                     this.handleMovementViolation(event, BadPackets.MovementCategory.FLY);
                  }

                  if (value > 0) {
                     this.stepCounts.remove(player.getUniqueId());
                  }
                  break;
               case 1:
                  if (!NumberUtil.isWithinRange(y, 0.33, 0.02)) {
                     if (!this.teleportExemptPlayers.contains(player.getUniqueId())) {
                        this.teleportExemptPlayers.add(player.getUniqueId());
                     } else if (this.recordViolation(
                        player.getUniqueId(),
                        1,
                        HackType.NORMALMOVEMENTS,
                        player.getName() + " failed NormalMovements: moved unexpectedly f:jump:1 (dY:" + y + ")"
                     )) {
                        this.handleMovementViolation(event, BadPackets.MovementCategory.FLY);
                     }
                  }
                  break;
               case 2:
                  if (!NumberUtil.isWithinRange(y, 0.24, 0.02)) {
                     if (!this.teleportExemptPlayers.contains(player.getUniqueId())) {
                        this.teleportExemptPlayers.add(player.getUniqueId());
                     } else if (this.recordViolation(
                        player.getUniqueId(),
                        1,
                        HackType.NORMALMOVEMENTS,
                        player.getName() + " failed NormalMovements: moved unexpectedly f:jump:2 (dY:" + y + ")"
                     )) {
                        this.handleMovementViolation(event, BadPackets.MovementCategory.FLY);
                     }
                  }
                  break;
               case 3:
                  if (!NumberUtil.isWithinRange(y, 0.16, 0.02)) {
                     if (!this.teleportExemptPlayers.contains(player.getUniqueId())) {
                        this.teleportExemptPlayers.add(player.getUniqueId());
                     } else if (this.recordViolation(
                        player.getUniqueId(),
                        1,
                        HackType.NORMALMOVEMENTS,
                        player.getName() + " failed NormalMovements: moved unexpectedly f:jump:3 (dY:" + y + ")"
                     )) {
                        this.handleMovementViolation(event, BadPackets.MovementCategory.FLY);
                     }
                  }
                  break;
               case 4:
                  if (!NumberUtil.isWithinRange(y, 0.08, 0.02)) {
                     if (!this.teleportExemptPlayers.contains(player.getUniqueId())) {
                        this.teleportExemptPlayers.add(player.getUniqueId());
                     } else if (this.recordViolation(
                        player.getUniqueId(),
                        1,
                        HackType.NORMALMOVEMENTS,
                        player.getName() + " failed NormalMovements: moved unexpectedly f:jump:4 (dY:" + y + ")"
                     )) {
                        this.handleMovementViolation(event, BadPackets.MovementCategory.FLY);
                     }
                  }
                  break;
               case 5:
               case 6:
               case 7:
               case 8:
               case 9:
               case 10:
               case 11:
               default:
                  if (y > 0.27) {
                     if (!this.teleportExemptPlayers.contains(player.getUniqueId())) {
                        this.teleportExemptPlayers.add(player.getUniqueId());
                     } else if (this.recordViolation(
                        player.getUniqueId(),
                        1,
                        HackType.NORMALMOVEMENTS,
                        player.getName() + " failed NormalMovements: moved unexpectedly f:jump:" + value2 + "(dY:" + y + ")"
                     )) {
                        this.handleMovementViolation(event, BadPackets.MovementCategory.FLY);
                     }
                  }
                  break;
               case 12:
                  if (NumberUtil.isWithinRange(y, JUMP_VELOCITIES[0], 0.02)) {
                     this.stepCounts.put(player.getUniqueId(), 1);
                  }
            }
         }
      }

      if (this.fStep && flag && flag2) {
         if (value <= 1) {
            if (y > 0.5625) {
               if (!this.teleportExemptPlayers.contains(player.getUniqueId())) {
                  this.teleportExemptPlayers.add(player.getUniqueId());
               } else if (this.recordViolation(
                  player.getUniqueId(), 1, HackType.NORMALMOVEMENTS, player.getName() + " failed NormalMovements: moved unexpectedly f:step:0 (dY:" + y + ")"
               )) {
                  this.handleMovementViolation(event, BadPackets.MovementCategory.FLY);
               }
            }
         } else if (y > value / 10.0 + JUMP_VELOCITIES[0]) {
            if (!this.teleportExemptPlayers.contains(player.getUniqueId())) {
               this.teleportExemptPlayers.add(player.getUniqueId());
            } else if (this.recordViolation(
               player.getUniqueId(), 1, HackType.NORMALMOVEMENTS, player.getName() + " failed NormalMovements: moved unexpectedly f:step:0 (dY:" + y + ")"
            )) {
               this.handleMovementViolation(event, BadPackets.MovementCategory.FLY);
            }
         }
      }

      if (this.fFloat) {
         if (!flag && !flag2 && NumberUtil.isWithinRange(0.0, y, 0.1) && !flag3) {
            if (!this.jumpTickCounts.containsKey(player.getUniqueId())) {
               this.jumpTickCounts.put(player.getUniqueId(), 0);
            }

            int value3 = this.jumpTickCounts.get(player.getUniqueId());
            this.jumpTickCounts.put(player.getUniqueId(), value3 + 1);
            if (value3 > 2) {
               if (!this.teleportExemptPlayers.contains(player.getUniqueId())) {
                  this.teleportExemptPlayers.add(player.getUniqueId());
               } else if (this.recordViolation(
                  player.getUniqueId(),
                  1,
                  HackType.NORMALMOVEMENTS,
                  player.getName() + " failed NormalMovements: moved unexpectedly f:float:" + value3 + " (dY:" + y + ")"
               )) {
                  if (value3 < 4) {
                     this.handleMovementViolation(event, BadPackets.MovementCategory.SPEED);
                  } else {
                     this.handleMovementViolation(event, BadPackets.MovementCategory.GLIDE);
                  }
               }
            }
         } else {
            this.jumpTickCounts.remove(player.getUniqueId());
         }
      }

      if (this.fDy && this.expectedMaxY.containsKey(player.getUniqueId()) && !flag3) {
         double d = this.expectedMaxY.get(player.getUniqueId());
         if (player.getVelocity().getY() > -0.4 && d > -0.2 && y < -0.6) {
            if (!this.teleportExemptPlayers.contains(player.getUniqueId())) {
               this.teleportExemptPlayers.add(player.getUniqueId());
            } else if (this.recordViolation(
               player.getUniqueId(), 1, HackType.NORMALMOVEMENTS, player.getName() + " failed NormalMovements: moved unexpectedly f:dy:- (dY:" + y + ")"
            )) {
               this.handleMovementViolation(event, BadPackets.MovementCategory.GLIDE);
            }
         }
      }

      this.expectedMaxY.put(player.getUniqueId(), y);
   }

   @Override
   public boolean shouldProcessPlayerMove(PlayerMoveEvent event) {
      if (!event.getPlayer().isFlying()
         && event.getPlayer().getGameMode() != GameMode.CREATIVE
         && event.getPlayer().getGameMode() != GameMode.SPECTATOR
         && event.getPlayer().getVehicle() == null) {
         for (Entity entity : event.getPlayer().getNearbyEntities(2.0, 2.0, 2.0)) {
            if (entity.getType() == EntityType.BOAT || entity.getType() == EntityType.MINECART) {
               return false;
            }
         }

         return !this.plugin.combatLogoutListener.hasRecentDamage(event.getPlayer())
            && !this.plugin.playerState.hasActiveVelocity(event.getPlayer())
            && event.getPlayer().getVelocity().getY() < 0.5
            && Math.abs(event.getPlayer().getVelocity().getX()) < 0.5
            && Math.abs(event.getPlayer().getVelocity().getZ()) < 0.5;
      } else {
         return false;
      }
   }

   private void handleMovementViolation(PlayerMoveEvent event, BadPackets.MovementCategory movementCategory) {
      if (this.teleport) {
         switch (MovementCategorySwitch.SWITCH_TABLE[movementCategory.ordinal()]) {
            case 1:
               this.plugin.movementDispatcher.setTeleportExempt(this, event.getPlayer());
               event.getPlayer().teleport(this.plugin.movementDispatcher.getSafeLocation(event.getPlayer()), TeleportCause.UNKNOWN);
               break;
            case 2:
               Speed speed = (Speed)this.plugin.checkManager.getDetector(HackType.SPEED);
               if (speed != null) {
                  event.setTo(speed.getOrUpdateSetbackLocation(event.getPlayer(), this.plugin.movementDispatcher.getSafeLocation(event.getPlayer())));
               } else {
                  event.setTo(event.getFrom());
               }
               break;
            case 3:
               Glide.applySetback(event.getPlayer(), event, this, this.plugin);
         }
      }
   }

   public void clearCaches() {
      this.stepCounts.clear();
      this.jumpTickCounts.clear();
      this.expectedMaxY.clear();
   }

   public static boolean isNearClimbable(Player player) {
      Block block = player.getLocation().getBlock();

      for (BlockFace face : BlockFace.values()) {
         if (block.getRelative(face).isLiquid()) {
            return true;
         }
      }

      return false;
   }

   public static boolean isStandingOnSlime(Player player) {
      return player.getLocation().subtract(0.0, 1.0, 0.0).getBlock().getType() == Material.SLIME_BLOCK;
   }

   public static boolean isNearPistonOrWeb(Player player) {
      HashSet<Block> set = new HashSet<>();
      RelativeBlockCache relativeBlockCache = new RelativeBlockCache(player.getLocation());
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
         if (isPistonWebOrSnow(block)) {
            return true;
         }

         Block block2 = block.getRelative(BlockFace.UP);
         if (block2.getType() == Material.WEB || block2.getRelative(BlockFace.UP).getType() == Material.WEB) {
            return true;
         }
      }

      return false;
   }

   private static boolean isPistonWebOrSnow(Block block) {
      return block.getType() == Material.PISTON_BASE
         || block.getType() == Material.PISTON_EXTENSION
         || block.getType() == Material.PISTON_MOVING_PIECE
         || block.getType() == Material.PISTON_STICKY_BASE
         || block.getType() == Material.WEB
         || block.getType() == Material.SNOW;
   }


   static Set getTeleportExemptPlayers(NormalMovements normalMovements) {
      return normalMovements.teleportExemptPlayers;
   }

   private static class DecayViolationsTask implements Runnable {
      final NormalMovements normalMovements;

      DecayViolationsTask(NormalMovements normalMovements) {
         this.normalMovements = normalMovements;
      }

      @Override
      public void run() {
         for (UUID uuid : this.normalMovements.violationLevels.keySet()) {
            this.normalMovements.decreaseViolationLevel(uuid, 1);
         }
      }
   }

   private static class ClearBypassTask implements Runnable {
      final NormalMovements normalMovements;

      ClearBypassTask(NormalMovements normalMovements) {
         this.normalMovements = normalMovements;
      }

      @Override
      public void run() {
         NormalMovements.getTeleportExemptPlayers(this.normalMovements).clear();
      }
   }

   class MovementCategorySwitch {
      static final int[] SWITCH_TABLE = new int[BadPackets.MovementCategory.values().length];

      static {
         try {
            SWITCH_TABLE[BadPackets.MovementCategory.FLY.ordinal()] = 1;
         } catch (NoSuchFieldError ex) {
         }

         try {
            SWITCH_TABLE[BadPackets.MovementCategory.SPEED.ordinal()] = 2;
         } catch (NoSuchFieldError ex2) {
         }

         try {
            SWITCH_TABLE[BadPackets.MovementCategory.GLIDE.ordinal()] = 3;
         } catch (NoSuchFieldError ex3) {
         }
      }
   }
}
