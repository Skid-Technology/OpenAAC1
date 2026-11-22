package me.konsolas.aac.detectors.movement;

import com.comphenix.protocol.PacketType.Play.Client;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import me.konsolas.aac.AAC;
import me.konsolas.aac.api.HackType;
import me.konsolas.aac.detectors.Detector;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

public final class BadPackets extends Detector {
   private int maxMoves = 50;
   private int cancelThreshold = 10;
   private ConcurrentMap<UUID, Integer> packetCounts = new ConcurrentHashMap<>();
   private Set<UUID> throttledPlayers = new HashSet<>();

   public BadPackets(AAC plugin) {
      super(plugin);
      this.maxMoves = this.plugin.getConfig().getInt("badpackets.max_moves", this.maxMoves);
      this.cancelThreshold = this.plugin.getConfig().getInt("badpackets.cancel_threshold", this.cancelThreshold);
      this.scheduledTaskIds.add(Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, new PacketRateCheckTask(this), 3L, 3L));
      MovementPacketListener movementPacketListener = new MovementPacketListener(this, this.plugin, ListenerPriority.LOWEST, Client.FLYING, Client.POSITION, Client.POSITION_LOOK, Client.LOOK);
      this.packetListeners.add(movementPacketListener);
      ProtocolLibrary.getProtocolManager().addPacketListener(movementPacketListener);
   }

   @EventHandler(priority = EventPriority.MONITOR)
   public void onBadPacketsPlayerJoin(PlayerJoinEvent event) {
      this.violationLevels.put(event.getPlayer().getUniqueId(), 0);
      this.packetCounts.put(event.getPlayer().getUniqueId(), 0);
   }

   static ConcurrentMap<UUID, Integer> getPacketCounts(BadPackets badPackets) {
      return badPackets.packetCounts;
   }

   static int getMaxMoves(BadPackets badPackets) {
      return badPackets.maxMoves;
   }


   static Set getThrottledPlayers(BadPackets badPackets) {
      return badPackets.throttledPlayers;
   }


   static int getCancelThreshold(BadPackets badPackets) {
      return badPackets.cancelThreshold;
   }

   private static class ClearCancelThrottleTask implements Runnable {
      final UUID playerId;
      final PacketRateCheckTask rateTask;

      ClearCancelThrottleTask(PacketRateCheckTask packetRateCheckTask, UUID uuid) {
         this.rateTask = packetRateCheckTask;
         this.playerId = uuid;
      }

      @Override
      public void run() {
         BadPackets.getThrottledPlayers(this.rateTask.badPackets).remove(this.playerId);
      }
   }

   private static class PacketRateCheckTask implements Runnable {
      final BadPackets badPackets;

      PacketRateCheckTask(BadPackets badPackets) {
         this.badPackets = badPackets;
      }

      @Override
      public void run() {
         for (Entry<UUID, Integer> entry : BadPackets.getPacketCounts(this.badPackets).entrySet()) {
            if (BadPackets.getPacketCounts(this.badPackets).containsKey(entry.getKey())) {
               Player player = Bukkit.getPlayer((UUID)entry.getKey());
               if (player == null) {
                  BadPackets.getPacketCounts(this.badPackets).remove(entry.getKey());
               } else {
                  if ((Integer)entry.getValue() > BadPackets.getMaxMoves(this.badPackets) && !this.badPackets.getPlugin().checkManager.isPlayerExempt(player)) {
                     if (!BadPackets.getThrottledPlayers(this.badPackets).contains(entry.getKey())) {
                        this.badPackets.recordViolation((UUID)entry.getKey(), ((Integer)entry.getValue() - BadPackets.getMaxMoves(this.badPackets)) / 10, HackType.BADPACKETS, player.getName() + " failed BadPackets: sent " + BadPackets.getPacketCounts(this.badPackets).get(entry.getKey()) + " moves in 3 ticks.");
                     } else {
                        this.badPackets.recordViolation((UUID)entry.getKey(), ((Integer)entry.getValue() - BadPackets.getMaxMoves(this.badPackets)) / 50, HackType.BADPACKETS, player.getName() + " failed BadPackets: sent " + BadPackets.getPacketCounts(this.badPackets).get(entry.getKey()) + " moves in 3 ticks.");
                        BadPackets.getThrottledPlayers(this.badPackets).add(entry.getKey());
                        UUID uuid = (UUID)entry.getKey();
                        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.badPackets.getPlugin(), new ClearCancelThrottleTask(this, uuid), 50L);
                     }
                  } else {
                     this.badPackets.decreaseViolationLevel((UUID)entry.getKey(), 1);
                  }

                  BadPackets.getPacketCounts(this.badPackets).put(entry.getKey(), 0);
               }
            }
         }
      }
   }

   private static class MovementPacketListener extends PacketAdapter {
      final BadPackets badPackets;

      MovementPacketListener(BadPackets badPackets, Plugin plugin, ListenerPriority listenerPriority, PacketType... packetTypeArray) {
         super(plugin, listenerPriority, packetTypeArray);
         this.badPackets = badPackets;
      }

      public void onPacketReceiving(PacketEvent event) {
         try {
            if (event.getPacketType() == Client.FLYING
               || event.getPacketType() == Client.POSITION
               || event.getPacketType() == Client.POSITION_LOOK
               || event.getPacketType() == Client.LOOK) {
               if (((AAC)this.plugin).checkManager.isPlayerExempt(event.getPlayer())) {
                  return;
               }

               BadPackets.getPacketCounts(this.badPackets).putIfAbsent(event.getPlayer().getUniqueId(), 0);
               BadPackets.getPacketCounts(this.badPackets).put(event.getPlayer().getUniqueId(), (Integer)BadPackets.getPacketCounts(this.badPackets).get(event.getPlayer().getUniqueId()) + 1);
               if ((Integer)BadPackets.getPacketCounts(this.badPackets).get(event.getPlayer().getUniqueId()) > BadPackets.getCancelThreshold(this.badPackets)) {
                  ((AAC)this.plugin)
                     .verboseLogger
                     .logPlayerDebug(
                        event.getPlayer(),
                        "(BadPackets) PacketPlayInFlying cancelled packets[m:"
                           + BadPackets.getCancelThreshold(this.badPackets)
                           + ",r:"
                           + BadPackets.getPacketCounts(this.badPackets).get(event.getPlayer().getUniqueId())
                           + "]"
                     );
                  event.setCancelled(true);
               }
            }
         } catch (NullPointerException ex) {
         }
      }
   }

   public enum MovementCategory {
      SPEED,
      FLY,
      GLIDE;

      private static final MovementCategory[] VALUES = new MovementCategory[]{SPEED, FLY, GLIDE};
   }
}
