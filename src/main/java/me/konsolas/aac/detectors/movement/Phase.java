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
import me.konsolas.aac.util.blocks.MovementObstacleChecks;
import me.konsolas.aac.util.blocks.RelativeBlockCache;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.material.Directional;
import org.bukkit.material.Door;
import org.bukkit.material.Gate;
import org.bukkit.material.TrapDoor;

public final class Phase extends Detector implements MoveListener {
   private Set<UUID> exemptPlayers = new HashSet<>();
   private Map<UUID, Location> lastSafeLocations = new HashMap<>();
   private Set<UUID> frozenPlayers = new HashSet<>();
   private Map<UUID, Block> lastCollidedBlocks = new HashMap<>();
   private Set<UUID> ignoredTeleportPlayers = new HashSet<>();
   private int freezeTime = 500;

   public Phase(AAC plugin) {
      super(plugin);
      this.freezeTime = this.plugin.getConfig().getInt("phase.freeze_time", this.freezeTime);
      this.scheduledTaskIds.add(Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, new DecayViolationsTask(this), 69L, 47L));
   }

   @Override
   public void setCurrentTick(long ticks) {
      for (Player player : Bukkit.getOnlinePlayers()) {
         if (!this.plugin.checkManager.isPlayerExempt(player) && !player.getGameMode().equals(GameMode.SPECTATOR) && player.getVehicle() == null) {
            UUID uuid = player.getUniqueId();
            Location location = this.lastSafeLocations.containsKey(uuid) ? this.lastSafeLocations.get(uuid) : player.getLocation();
            Location location2 = player.getLocation();
            if (location != null && !location.equals(location2)) {
               if (location.getWorld() == location2.getWorld()
                  && location.distance(location2) > 10.0
                  && this.recordViolation(player.getUniqueId(), 1, HackType.PHASE, player.getName() + " failed Phase: moved too far away from safe location.")) {
                  this.lastCollidedBlocks.remove(player.getUniqueId());
                  this.plugin.movementDispatcher.setTeleportExempt(this, player);
                  player.teleport(location, TeleportCause.UNKNOWN);
               } else {
                  Block block = this.findCollidedBlock(location, location2);
                  if (block == null) {
                     this.lastSafeLocations.put(uuid, location2);
                  } else if ((
                        !location2.getBlock().equals(location.getBlock().getRelative(BlockFace.UP)) && !location2.getBlock().equals(location.getBlock())
                           || !MovementObstacleChecks.isPassableBlock(location.getBlock().getRelative(BlockFace.UP))
                     )
                     && this.recordViolation(
                        player.getUniqueId(),
                        1,
                        HackType.PHASE,
                        player.getName() + " failed Phase: tried to move through " + block.getType() + " at " + block.getLocation().toVector().toString()
                     )) {
                     this.lastCollidedBlocks.remove(player.getUniqueId());
                     this.plugin.movementDispatcher.setTeleportExempt(this, player);
                     player.teleport(this.lastSafeLocations.get(uuid), TeleportCause.UNKNOWN);
                  }
               }
            }
         } else {
            this.lastSafeLocations.put(player.getUniqueId(), player.getLocation());
         }
      }
   }

   private Block findCollidedBlock(Location location, Location location2) {
      if (location.getWorld() != location2.getWorld()) {
         return null;
      }

      int value = Math.max(location.getBlockX(), location2.getBlockX());
      int value2 = Math.min(location.getBlockX(), location2.getBlockX());
      int value3 = Math.max(location.getBlockY(), location2.getBlockY()) + 1;
      int value4 = Math.min(location.getBlockY(), location2.getBlockY());
      int value5 = Math.max(location.getBlockZ(), location2.getBlockZ());
      int value6 = Math.min(location.getBlockZ(), location2.getBlockZ());
      if (value3 > 256) {
         value = 256;
      }

      if (value4 > 256) {
         value4 = 256;
      }

      for (int value7 = value2; value7 <= value; value7++) {
         for (int value8 = value6; value8 <= value5; value8++) {
            for (int value9 = value4; value9 <= value3; value9++) {
               Block block = location.getWorld().getBlockAt(value7, value9, value8);
               if ((value9 != value4 || location.getBlockY() == location2.getBlockY()) && this.passesThroughBlock(block, location, location2)) {
                  return block;
               }
            }
         }
      }

      return null;
   }

   private boolean passesThroughBlock(Block block, Location location, Location location2) {
      if (MovementObstacleChecks.isPassableBlock(block)) {
         return false;
      }

      double x = Math.max(location.getX(), location2.getX());
      double x2 = Math.min(location.getX(), location2.getX());
      double y = Math.max(location.getY(), location2.getY()) + 1.8;
      double y2 = Math.min(location.getY(), location2.getY());
      double z = Math.max(location.getZ(), location2.getZ());
      double z2 = Math.min(location.getZ(), location2.getZ());
      double d = block.getLocation().getBlockX() + 1;
      double d2 = block.getLocation().getBlockX();
      double d3 = block.getLocation().getBlockY() + 2;
      double d4 = block.getLocation().getBlockY();
      double d5 = block.getLocation().getBlockZ() + 1;
      double d6 = block.getLocation().getBlockZ();
      if (d4 > y2) {
         d3--;
      }

      if (MovementObstacleChecks.isCarpetOrPressurePlate(block)) {
         Door door = (Door)block.getType().getNewData(block.getData());
         if (door.isTopHalf()) {
            return false;
         }

         BlockFace face = door.getFacing();
         if (door.isOpen()) {
            Block block2 = block.getRelative(BlockFace.UP);
            if (!MovementObstacleChecks.isCarpetOrPressurePlate(block2)) {
               return false;
            }

            boolean flag = (block2.getData() & 1) == 1;
            if (face == BlockFace.NORTH) {
               face = flag ? BlockFace.WEST : BlockFace.EAST;
            } else if (face == BlockFace.EAST) {
               face = flag ? BlockFace.NORTH : BlockFace.SOUTH;
            } else if (face == BlockFace.SOUTH) {
               face = flag ? BlockFace.EAST : BlockFace.WEST;
            } else {
               face = flag ? BlockFace.SOUTH : BlockFace.NORTH;
            }
         }

         if (face == BlockFace.WEST) {
            d -= 0.8;
         }

         if (face == BlockFace.EAST) {
            d2 += 0.8;
         }

         if (face == BlockFace.NORTH) {
            d5 -= 0.8;
         }

         if (face == BlockFace.SOUTH) {
            d6 += 0.8;
         }
      } else if (MovementObstacleChecks.isFullCollisionBlock(block)) {
         boolean flag2 = ((Gate)block.getType().getNewData(block.getData())).isOpen();
         if (flag2) {
            return false;
         }

         BlockFace face2 = ((Directional)block.getType().getNewData(block.getData())).getFacing();
         if (face2 != BlockFace.NORTH && face2 != BlockFace.SOUTH) {
            d5 -= 0.2;
            d6 += 0.2;
         } else {
            d -= 0.2;
            d2 += 0.2;
         }
      } else if (MovementObstacleChecks.isDoorOrTrapdoor(block)) {
         TrapDoor trapDoor = (TrapDoor)block.getType().getNewData(block.getData());
         if (trapDoor.isOpen()) {
            return false;
         }

         if (trapDoor.isInverted()) {
            d4 += 0.85;
         } else {
            d3 -= d4 > y2 ? 0.85 : 1.85;
         }
      } else if (MovementObstacleChecks.isWaterloggedOrPartial(block)) {
         d -= 0.2;
         d2 += 0.2;
         d5 -= 0.2;
         d6 += 0.2;
         if (x > d && x2 > d && z > d5 && z2 > d5
            || x < d2 && x2 < d2 && z > d5 && z2 > d5
            || x > d && x2 > d && z < d6 && z2 < d6
            || x < d2 && x2 < d2 && z < d6 && z2 < d6) {
            return false;
         }

         if (block.getRelative(BlockFace.EAST).getType() == block.getType()) {
            d += 0.2;
         }

         if (block.getRelative(BlockFace.WEST).getType() == block.getType()) {
            d2 -= 0.2;
         }

         if (block.getRelative(BlockFace.SOUTH).getType() == block.getType()) {
            d5 += 0.2;
         }

         if (block.getRelative(BlockFace.NORTH).getType() == block.getType()) {
            d6 -= 0.2;
         }
      }

      boolean x3 = location.getX() < location2.getX();
      boolean y3 = location.getY() < location2.getY();
      boolean z3 = location.getZ() < location2.getZ();
      return x2 != x
            && y2 <= d3
            && y >= d4
            && z2 <= d5
            && z >= d6
            && (x3 && x2 <= d2 && x >= d2 || !x3 && x2 <= d && x >= d)
         || y2 != y
            && x2 <= d
            && x >= d2
            && z2 <= d5
            && z >= d6
            && (y3 && y2 <= d4 && y >= d4 || !y3 && y2 <= d3 && y >= d3)
         || z2 != z
            && x2 <= d
            && x >= d2
            && y2 <= d3
            && y >= d4
            && (z3 && z2 <= d6 && z >= d6 || !z3 && z2 <= d5 && z >= d5);
   }

   @Override
   public boolean shouldProcessPlayerMove(PlayerMoveEvent event) {
      return event.getPlayer().getVehicle() == null
         && event.getPlayer().getGameMode() != GameMode.SPECTATOR
         && event.getPlayer().getGameMode() != GameMode.CREATIVE
         && !this.exemptPlayers.contains(event.getPlayer().getUniqueId());
   }

   @Override
   public void onPlayerMove(PlayerMoveEvent event, boolean flag, boolean flag2, RelativeBlockCache relativeBlockCache, RelativeBlockCache relativeBlockCache2) {
      if (this.frozenPlayers.contains(event.getPlayer().getUniqueId())) {
         event.setTo(this.lastSafeLocations.get(event.getPlayer().getUniqueId()));
         this.frozenPlayers.remove(event.getPlayer().getUniqueId());
      } else {
         if (!this.lastSafeLocations.containsKey(event.getPlayer().getUniqueId())) {
            this.lastSafeLocations.put(event.getPlayer().getUniqueId(), event.getFrom());
         }
      }
   }

   @EventHandler
   public void onPlayerTeleport(PlayerTeleportEvent event) {
      this.clearCollisionHistory(event.getPlayer(), event.getTo());
   }

   @EventHandler
   public void onPlayerRespawn(PlayerRespawnEvent event) {
      this.clearCollisionHistory(event.getPlayer(), event.getRespawnLocation());
   }

   @EventHandler
   public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
      this.lastSafeLocations.remove(event.getPlayer().getUniqueId());
      this.lastCollidedBlocks.remove(event.getPlayer().getUniqueId());
   }

   private void clearCollisionHistory(Player player, Location location) {
      this.lastSafeLocations.remove(player.getUniqueId());
      this.lastCollidedBlocks.remove(player.getUniqueId());
   }

   public void clearCaches() {
      this.exemptPlayers.clear();
      this.lastSafeLocations.clear();
      this.lastCollidedBlocks.clear();
   }

   public boolean isFrozen(Player player) {
      return false;
   }


   private static class DecayViolationsTask implements Runnable {
      final Phase phase;

      DecayViolationsTask(Phase phase) {
         this.phase = phase;
      }

      @Override
      public void run() {
         for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            this.phase.decreaseViolationLevel(player.getUniqueId(), 1);
         }
      }
   }
}
