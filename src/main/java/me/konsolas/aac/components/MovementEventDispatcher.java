package me.konsolas.aac.components;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import me.konsolas.aac.AAC;
import me.konsolas.aac.detectors.Detector;
import me.konsolas.aac.detectors.MoveListener;
import me.konsolas.aac.detectors.movement.Glide;
import me.konsolas.aac.detectors.movement.Speed;
import me.konsolas.aac.util.blocks.MovementObstacleChecks;
import me.konsolas.aac.util.blocks.RelativeBlockCache;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.world.WorldUnloadEvent;

public class MovementEventDispatcher implements Listener, MoveListener {
   private AAC plugin;
   private final Map<UUID, Location> safeLocations = new ConcurrentHashMap<>();
   private final Map<UUID, PlayerMoveEvent> lastMoveEvents = new ConcurrentHashMap<>();
   private final Map<UUID, Detector> exemptDetectors = new ConcurrentHashMap<>();

   public MovementEventDispatcher(AAC plugin) {
      this.plugin = plugin;
   }

   public void setTeleportExempt(Detector detector, Player player) {
      this.exemptDetectors.put(player.getUniqueId(), detector);
   }

   public void clearTeleportExempt(Player player) {
      this.exemptDetectors.remove(player.getUniqueId());
   }

   @EventHandler(ignoreCancelled = false, priority = EventPriority.HIGHEST)
   public void onPlayerTeleport(PlayerTeleportEvent event) {
      if (this.exemptDetectors.containsKey(event.getPlayer().getUniqueId()) && event.getCause() == TeleportCause.UNKNOWN) {
         if (this.exemptDetectors.get(event.getPlayer().getUniqueId()) instanceof Speed || this.exemptDetectors.get(event.getPlayer().getUniqueId()) instanceof Glide) {
            this.safeLocations.remove(event.getPlayer().getUniqueId());
         }

         this.exemptDetectors.remove(event.getPlayer().getUniqueId());
      } else {
         if (event.getTo().equals(event.getFrom()) && event.getCause() == TeleportCause.UNKNOWN) {
            return;
         }

         this.safeLocations.put(event.getPlayer().getUniqueId(), event.getTo().clone());
      }
   }

   @EventHandler(ignoreCancelled = false, priority = EventPriority.MONITOR)
   public void onPlayerRespawn(PlayerRespawnEvent event) {
      this.safeLocations.remove(event.getPlayer().getUniqueId());
   }

   @EventHandler(ignoreCancelled = false, priority = EventPriority.MONITOR)
   public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
      this.safeLocations.remove(event.getPlayer().getUniqueId());
   }

   @EventHandler
   public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
      Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new DeferredTeleport(this, event), 40L);
   }

   public Detector getTeleportExempt(Player player) {
      return this.exemptDetectors.get(player.getUniqueId());
   }

   private void updateSafeOnGround(PlayerMoveEvent event, RelativeBlockCache relativeBlockCache) {
      if (MovementObstacleChecks.isPlayerUnobstructed(relativeBlockCache)) {
         if (this.safeLocations.containsKey(event.getPlayer().getUniqueId())) {
            if (event.getFrom().getWorld() != this.safeLocations.get(event.getPlayer().getUniqueId()).getWorld()
               || event.getFrom().distanceSquared(this.safeLocations.get(event.getPlayer().getUniqueId())) > 3.0
               || event.getFrom().getY() - this.safeLocations.get(event.getPlayer().getUniqueId()).getY() > 0.2) {
               this.safeLocations.put(event.getPlayer().getUniqueId(), event.getFrom().clone());
            } else if (MovementObstacleChecks.isFootPosClear(relativeBlockCache)) {
               this.safeLocations.put(event.getPlayer().getUniqueId(), event.getFrom().clone());
            }
         } else {
            this.safeLocations.put(event.getPlayer().getUniqueId(), event.getFrom().clone());
         }
      }
   }

   private void updateSafeOnApex(PlayerMoveEvent event, RelativeBlockCache relativeBlockCache) {
      if (this.lastMoveEvents.containsKey(event.getPlayer().getUniqueId())) {
         PlayerMoveEvent event2 = this.lastMoveEvents.get(event.getPlayer().getUniqueId());
         double y = event2.getTo().getY() - event2.getFrom().getY();
         double y2 = event.getTo().getY() - event.getFrom().getY();
         if (y < 0.0 && y2 > 0.0 && MovementObstacleChecks.isPlayerUnobstructed(relativeBlockCache)) {
            if (this.safeLocations.containsKey(event.getPlayer().getUniqueId())) {
               if (event.getFrom().distanceSquared(this.safeLocations.get(event.getPlayer().getUniqueId())) > 3.0) {
                  this.safeLocations.put(event.getPlayer().getUniqueId(), event.getFrom().clone());
               } else if (MovementObstacleChecks.isFootPosClear(relativeBlockCache)) {
                  this.safeLocations.put(event.getPlayer().getUniqueId(), event.getFrom().clone());
               }
            } else {
               this.safeLocations.put(event.getPlayer().getUniqueId(), event.getFrom().clone());
            }
         }
      }
   }

   private void updateSafeAirborne(PlayerMoveEvent event, RelativeBlockCache relativeBlockCache, RelativeBlockCache relativeBlockCache2) {
      if (this.safeLocations.containsKey(event.getPlayer().getUniqueId())) {
         if (event.getTo().getY() < this.safeLocations.get(event.getPlayer().getUniqueId()).getY()
            && Math.abs(event.getFrom().getY() - event.getTo().getY()) < 3.0
            && this.safeLocations.get(event.getPlayer().getUniqueId()).distance(event.getTo()) < 5.0) {
            if (MovementObstacleChecks.isPlayerUnobstructed(relativeBlockCache2)) {
               this.safeLocations.put(event.getPlayer().getUniqueId(), event.getTo().clone());
            }

            return;
         }

         this.lastMoveEvents.put(event.getPlayer().getUniqueId(), event);
      } else if (MovementObstacleChecks.isPlayerUnobstructed(relativeBlockCache)) {
         if (this.safeLocations.containsKey(event.getPlayer().getUniqueId())) {
            if (event.getFrom().getWorld() != this.safeLocations.get(event.getPlayer().getUniqueId()).getWorld()
               || event.getFrom().distanceSquared(this.safeLocations.get(event.getPlayer().getUniqueId())) > 3.0) {
               this.safeLocations.put(event.getPlayer().getUniqueId(), event.getPlayer().getLocation());
            } else if (MovementObstacleChecks.isFootPosClear(relativeBlockCache)) {
               this.safeLocations.put(event.getPlayer().getUniqueId(), event.getPlayer().getLocation());
            }
         } else {
            this.safeLocations.put(event.getPlayer().getUniqueId(), event.getPlayer().getLocation());
         }
      }
   }

   public Location getSafeLocation(Player player) {
      return this.safeLocations.get(player.getUniqueId()) != null ? this.safeLocations.get(player.getUniqueId()) : player.getLocation();
   }

   @Override
   public void onPlayerMove(PlayerMoveEvent event, boolean flag, boolean flag2, RelativeBlockCache relativeBlockCache, RelativeBlockCache relativeBlockCache2) {
      if (flag && flag2) {
         this.updateSafeOnGround(event, relativeBlockCache);
      } else {
         if (flag) {
            this.updateSafeOnApex(event, relativeBlockCache);
         } else if (flag2) {
            this.updateSafeAirborne(event, relativeBlockCache, relativeBlockCache2);
         }
      }
   }

   @Override
   public boolean shouldProcessPlayerMove(PlayerMoveEvent event) {
      if (event.getPlayer().isFlying()) {
         this.safeLocations.put(event.getPlayer().getUniqueId(), event.getPlayer().getLocation());
      }

      return true;
   }

   @EventHandler
   public void onWorldUnload(WorldUnloadEvent event) {
      HashSet<UUID> set = new HashSet<>();

      for (Entry<UUID, Location> entry : this.safeLocations.entrySet()) {
         if (((Location)entry.getValue()).getWorld().equals(event.getWorld())) {
            set.add(entry.getKey());
         }
      }

      for (UUID uuid : set) {
         this.safeLocations.remove(uuid);
      }
   }

   static Map getSafeLocations(MovementEventDispatcher movementEventDispatcher) {
      return movementEventDispatcher.safeLocations;
   }

   private static class DeferredTeleport implements Runnable {
      final PlayerToggleFlightEvent event;
      final MovementEventDispatcher dispatcher;

      DeferredTeleport(MovementEventDispatcher movementEventDispatcher, PlayerToggleFlightEvent event) {
         this.dispatcher = movementEventDispatcher;
         this.event = event;
      }

      @Override
      public void run() {
         MovementEventDispatcher.getSafeLocations(this.dispatcher).put(this.event.getPlayer().getUniqueId(), this.event.getPlayer().getLocation());
      }
   }
}
