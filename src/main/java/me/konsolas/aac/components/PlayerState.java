package me.konsolas.aac.components;

import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import me.konsolas.aac.AAC;
import me.konsolas.aac.internal.Destroyable;
import me.konsolas.aac.internal.packetwrapper.WrapperPlayServerEntityVelocity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

public class PlayerState implements Listener, Destroyable {
   private AAC plugin;
   private HashSet<UUID> recentLandings = new HashSet<>();
   private HashSet<UUID> recentTakeoffs = new HashSet<>();
   private Map<UUID, Long> lastArmSwingTimes = new HashMap<>();
   private Map<UUID, Long> lastRegenerationTimes = new HashMap<>();
   private Map<UUID, RegainReason> lastRegenerationReasons = new HashMap<>();
   private Map<UUID, Long> lastInteractionTimes = new HashMap<>();
   private ConcurrentMap<UUID, Vector> activeVelocities = new ConcurrentHashMap<>(32, 0.9F, 1);
   private ConcurrentMap<UUID, Long> lastVelocityTimes = new ConcurrentHashMap<>(32, 0.9F, 1);
   private final ConcurrentMap<UUID, Double> lastHorizontalVelocities = new ConcurrentHashMap<>(32, 0.9F, 1);
   private final ConcurrentMap<UUID, Long> lastVelocityPacketTimes = new ConcurrentHashMap<>(32, 0.9F, 1);
   private PacketListener velocityPacketListener;
   private Map<UUID, Location> previousLocations = new HashMap<>();

   public PlayerState(AAC plugin) {
      this.plugin = plugin;
      this.velocityPacketListener = new VelocityPacketListener(this, this.plugin, Server.ENTITY_VELOCITY);
      ProtocolLibrary.getProtocolManager().addPacketListener(this.velocityPacketListener);
   }

   public void recordMoveState(PlayerMoveEvent event, boolean flag, boolean flag2) {
      if (flag && !flag2) {
         this.recentTakeoffs.add(event.getPlayer().getUniqueId());
         Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new ClearTakeoffTask(this, event), 3L);
      } else if (!flag && flag2) {
         this.recentLandings.add(event.getPlayer().getUniqueId());
         Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new ClearLandingTask(this, event), 3L);
      }

      if (this.activeVelocities.containsKey(event.getPlayer().getUniqueId()) && System.currentTimeMillis() - this.getLastVelocityTime(event.getPlayer()) > 90L) {
         if (flag2 && flag) {
            this.activeVelocities.remove(event.getPlayer().getUniqueId());
         } else if (event.getPlayer().getVelocity().getY() == 0.0
            && event.getPlayer().getVelocity().getX() == 0.0
            && event.getPlayer().getVelocity().getZ() == 0.0) {
            this.activeVelocities.remove(event.getPlayer().getUniqueId());
         }
      }

      this.previousLocations.put(event.getPlayer().getUniqueId(), event.getFrom());
   }

   @EventHandler
   public void onPlayerVelocity(PlayerVelocityEvent event) {
      if (this.plugin.movementDispatcher.getTeleportExempt(event.getPlayer()) == null) {
         this.activeVelocities.put(event.getPlayer().getUniqueId(), event.getVelocity());
         this.lastVelocityTimes.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
      }
   }

   @EventHandler
   public void onPlayerAnimation(PlayerAnimationEvent event) {
      if (event.getAnimationType() == PlayerAnimationType.ARM_SWING) {
         this.lastArmSwingTimes.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
      }
   }

   @EventHandler(priority = EventPriority.MONITOR)
   public void onEntityRegainHealth(EntityRegainHealthEvent event) {
      if (event.getEntity().getType() == EntityType.PLAYER) {
         this.lastRegenerationTimes.put(event.getEntity().getUniqueId(), System.currentTimeMillis());
         this.lastRegenerationReasons.put(event.getEntity().getUniqueId(), event.getRegainReason());
      }
   }

   @EventHandler(priority = EventPriority.MONITOR)
   public void onPlayerInteract(PlayerInteractEvent event) {
      this.lastInteractionTimes.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
   }

   @EventHandler(priority = EventPriority.MONITOR)
   public void onWorldUnload(WorldUnloadEvent event) {
      HashSet<UUID> set = new HashSet<>();

      for (Entry<UUID, Location> entry : this.previousLocations.entrySet()) {
         if (((Location)entry.getValue()).getWorld().equals(event.getWorld())) {
            set.add(entry.getKey());
         }
      }

      for (UUID uuid : set) {
         this.previousLocations.remove(uuid);
      }
   }

   @EventHandler
   public void onPlayerTeleport(PlayerTeleportEvent event) {
      this.lastRegenerationTimes.put(event.getPlayer().getUniqueId(), 0L);
   }

   public void clearRecentTakeoff(Player player) {
      this.recentTakeoffs.remove(player.getUniqueId());
   }

   public boolean hasRecentlyLanded(Player player) {
      return this.recentLandings.contains(player.getUniqueId());
   }

   public boolean hasRecentlyTakenOff(Player player) {
      return this.recentTakeoffs.contains(player.getUniqueId());
   }

   public synchronized boolean hasActiveVelocity(Player player) {
      return this.activeVelocities.containsKey(player.getUniqueId());
   }

   public synchronized Vector getActiveVelocity(Player player) {
      return this.activeVelocities.get(player.getUniqueId());
   }

   public synchronized long getLastVelocityTime(Player player) {
      return this.lastVelocityTimes.get(player.getUniqueId()) != null ? this.lastVelocityTimes.get(player.getUniqueId()) : 0L;
   }

   public long getLastArmSwingTime(Player player) {
      try {
         return this.lastArmSwingTimes.get(player.getUniqueId());
      } catch (NullPointerException ex) {
         return 0L;
      }
   }

   public long getLastRegenerationTime(Player player) {
      try {
         return this.lastRegenerationTimes.get(player.getUniqueId());
      } catch (NullPointerException ex) {
         return 0L;
      }
   }

   public RegainReason getLastRegenerationReason(Player player) {
      try {
         return this.lastRegenerationReasons.get(player.getUniqueId());
      } catch (NullPointerException ex) {
         return RegainReason.CUSTOM;
      }
   }

   public long getLastInteractionTime(Player player) {
      try {
         return this.lastInteractionTimes.get(player.getUniqueId());
      } catch (NullPointerException ex) {
         return 0L;
      }
   }

   public synchronized double getLastHorizontalVelocity(Player player) {
      try {
         return this.lastHorizontalVelocities.get(player.getUniqueId());
      } catch (NullPointerException ex) {
         return 0.0;
      }
   }

   public synchronized long getLastVelocityPacketTime(Player player) {
      try {
         return this.lastVelocityPacketTimes.get(player.getUniqueId());
      } catch (NullPointerException ex) {
         return Long.MIN_VALUE;
      }
   }

   @Override
   public void destroy() {
      ProtocolLibrary.getProtocolManager().removePacketListener(this.velocityPacketListener);
   }

   static ConcurrentMap getLastHorizontalVelocities(PlayerState playerState) {
      return playerState.lastHorizontalVelocities;
   }

   static ConcurrentMap getLastVelocityPacketTimes(PlayerState playerState) {
      return playerState.lastVelocityPacketTimes;
   }

   static HashSet getRecentTakeoffs(PlayerState playerState) {
      return playerState.recentTakeoffs;
   }

   static HashSet getRecentLandings(PlayerState playerState) {
      return playerState.recentLandings;
   }

   private static class VelocityPacketListener extends PacketAdapter {
      final PlayerState playerState;

      VelocityPacketListener(PlayerState playerState, Plugin plugin, PacketType... packetTypeArray) {
         super(plugin, packetTypeArray);
         this.playerState = playerState;
      }

      public void onPacketSending(PacketEvent event) {
         WrapperPlayServerEntityVelocity wrapperPlayServerEntityVelocity = new WrapperPlayServerEntityVelocity(event.getPacket());
         Vector vector = wrapperPlayServerEntityVelocity.getVelocity();
         double x = Math.sqrt(Math.pow(vector.getX(), 2.0) + Math.pow(vector.getZ(), 2.0));
         if (wrapperPlayServerEntityVelocity.getEntityID() == event.getPlayer().getEntityId()) {
            PlayerState.getLastHorizontalVelocities(this.playerState).put(event.getPlayer().getUniqueId(), x);
            PlayerState.getLastVelocityPacketTimes(this.playerState).put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
         }
      }
   }

   private static class ClearTakeoffTask implements Runnable {
      final PlayerMoveEvent event;
      final PlayerState playerState;

      ClearTakeoffTask(PlayerState playerState, PlayerMoveEvent event) {
         this.playerState = playerState;
         this.event = event;
      }

      @Override
      public void run() {
         PlayerState.getRecentTakeoffs(this.playerState).remove(this.event.getPlayer().getUniqueId());
      }
   }

   private static class ClearLandingTask implements Runnable {
      final PlayerMoveEvent event;
      final PlayerState playerState;

      ClearLandingTask(PlayerState playerState, PlayerMoveEvent event) {
         this.playerState = playerState;
         this.event = event;
      }

      @Override
      public void run() {
         PlayerState.getRecentLandings(this.playerState).remove(this.event.getPlayer().getUniqueId());
      }
   }
}
