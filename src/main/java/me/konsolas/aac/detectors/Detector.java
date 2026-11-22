package me.konsolas.aac.detectors;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import me.konsolas.aac.AAC;
import me.konsolas.aac.api.HackType;
import me.konsolas.aac.api.PlayerViolationEvent;
import me.konsolas.aac.internal.Destroyable;
import me.konsolas.aac.util.Ping;
import me.konsolas.aac.util.TPS;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public abstract class Detector implements Listener, Destroyable {
   protected long currentTick = 0L;
   protected final AAC plugin;
   public Map<UUID, Integer> violationLevels = new HashMap<>();
   public Map<UUID, Integer> recentViolationCounts = new HashMap<>();
   public Map<UUID, Integer> totalViolationCounts = new HashMap<>();
   protected List<Integer> scheduledTaskIds = new ArrayList<>();
   protected List<PacketListener> packetListeners = new ArrayList<>();

   public Detector(AAC plugin) {
      this.initializeOnlinePlayers();
      this.plugin = plugin;
   }

   public AAC getPlugin() {
      return this.plugin;
   }

   public int getViolationLevel(Player player) {
      return this.violationLevels.get(player.getUniqueId()) != null ? this.violationLevels.get(player.getUniqueId()) : 0;
   }

   public int getRecentViolationCount(Player player) {
      if (player == null) {
         return 0;
      } else {
         return this.recentViolationCounts.get(player.getUniqueId()) != null ? this.recentViolationCounts.get(player.getUniqueId()) : 0;
      }
   }

   public int getTotalViolationCount(Player player) {
      return this.totalViolationCounts.get(player.getUniqueId()) != null ? this.totalViolationCounts.get(player.getUniqueId()) : 0;
   }

   public final void initializeOnlinePlayers() {
      for (Player player : Bukkit.getServer().getOnlinePlayers()) {
         this.violationLevels.put(player.getUniqueId(), 0);
         this.recentViolationCounts.put(player.getUniqueId(), 0);
      }
   }

   @Override
   public void destroy() {
      for (int i : this.scheduledTaskIds) {
         Bukkit.getServer().getScheduler().cancelTask(i);
      }

      for (PacketListener packetListener : this.packetListeners) {
         ProtocolLibrary.getProtocolManager().removePacketListener(packetListener);
      }
   }

   public boolean recordViolation(UUID uuid, int i, HackType hackType, String str) {
      Player player = Bukkit.getPlayer(uuid);
      PlayerViolationEvent event = new PlayerViolationEvent(player, hackType, this.getRecentViolationCount(Bukkit.getPlayer(uuid)), str);
      Bukkit.getServer().getPluginManager().callEvent(event);
      if (!event.isCancelled()) {
         if (!this.recentViolationCounts.containsKey(uuid)) {
            this.recentViolationCounts.put(uuid, 1);
         }

         if (!this.totalViolationCounts.containsKey(uuid)) {
            this.totalViolationCounts.put(uuid, 1);
         }

         if (!this.violationLevels.containsKey(uuid)) {
            this.violationLevels.put(uuid, 1);
         }

         this.violationLevels.put(uuid, this.violationLevels.get(uuid) + i);
         this.recentViolationCounts.put(uuid, this.recentViolationCounts.get(uuid) + 1);
         Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new ExpireRecentViolationTask(this, uuid), 1200L);
         this.totalViolationCounts.put(uuid, this.totalViolationCounts.get(uuid) + 1);
         this.plugin
            .verboseLogger
            .logViolation(str + " [ping:" + Ping.getPing(player) + " tps:" + Math.round(TPS.getTps() * 100.0) / 100.0 + "] VL:" + this.violationLevels.get(uuid));
         return true;
      } else {
         return false;
      }
   }

   public void decreaseViolationLevel(UUID uuid, int i) {
      if (this.violationLevels.containsKey(uuid)) {
         this.violationLevels.put(uuid, this.violationLevels.get(uuid) - i < 0 ? 0 : this.violationLevels.get(uuid) - i);
      } else {
         this.violationLevels.put(uuid, 0);
      }
   }

   public void setViolationLevel(Player player, int i) {
      this.violationLevels.put(player.getUniqueId(), i);
   }

   public void onPlayerJoin(PlayerJoinEvent event) {
      this.violationLevels.put(event.getPlayer().getUniqueId(), 0);
      this.recentViolationCounts.put(event.getPlayer().getUniqueId(), 0);
   }

   public void onPlayerQuit(PlayerQuitEvent event) {
      this.violationLevels.remove(event.getPlayer().getUniqueId());
      this.recentViolationCounts.put(event.getPlayer().getUniqueId(), 0);
   }

   public void onPlayerKick(PlayerKickEvent event) {
      this.violationLevels.remove(event.getPlayer().getUniqueId());
      this.recentViolationCounts.put(event.getPlayer().getUniqueId(), 0);
   }

   public void setCurrentTick(long ticks) {
      this.currentTick = ticks;
   }

   private static class ExpireRecentViolationTask implements Runnable {
      final UUID playerId;
      final Detector detector;

      ExpireRecentViolationTask(Detector detector, UUID uuid) {
         this.detector = detector;
         this.playerId = uuid;
      }

      @Override
      public void run() {
         this.detector.recentViolationCounts.put(this.playerId, this.detector.recentViolationCounts.get(this.playerId) - 1 < 0 ? 0 : this.detector.recentViolationCounts.get(this.playerId) - 1);
      }
   }
}
