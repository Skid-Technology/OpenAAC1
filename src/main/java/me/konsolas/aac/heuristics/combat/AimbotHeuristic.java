package me.konsolas.aac.heuristics.combat;

import com.comphenix.protocol.PacketType.Play.Client;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.wrappers.EnumWrappers.EntityUseAction;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import me.konsolas.aac.AAC;
import me.konsolas.aac.heuristics.base.Heuristic;
import me.konsolas.aac.internal.packetwrapper.WrapperPlayClientUseEntity;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

public class AimbotHeuristic extends Heuristic {
   private Map<UUID, RotationSnapshot> snapshots = new HashMap<>();
   private PacketListener packetListener;
   private int decayTaskId;
   private ExpiringMap<UUID, Boolean> recentAttackPlayers;
   private Map<UUID, Float> lastYawValues = new HashMap<>();
   private Map<UUID, Float> preAttackYawValues = new HashMap<>();

   public AimbotHeuristic(AAC plugin) {
      super(plugin);
      this.recentAttackPlayers = ExpiringMap.builder().expiration(12L, TimeUnit.MILLISECONDS).expirationPolicy(ExpirationPolicy.CREATED).build();

      for (Player player : Bukkit.getServer().getOnlinePlayers()) {
         this.snapshots.put(player.getUniqueId(), new RotationSnapshot(player));
      }

      this.packetListener = new AimbotPacketListener(this, this.plugin, Client.FLYING, Client.POSITION, Client.POSITION_LOOK, Client.USE_ENTITY, Client.LOOK);
      ProtocolLibrary.getProtocolManager().addPacketListener(this.packetListener);
      this.decayTaskId = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, new DecayScoreTask(this), 50L, 73L);
   }

   @Override
   public void destroy() {
      super.destroy();
      ProtocolLibrary.getProtocolManager().removePacketListener(this.packetListener);
      Bukkit.getServer().getScheduler().cancelTask(this.decayTaskId);
   }

   @Override
   public int getDetectionChance(Player player) {
      double d = Math.pow(this.getHeuristicScore(player), 0.9) / 360.0;
      return (int)Math.round(d * 100.0 > 99.0 ? 99.0 : d * 100.0);
   }

   @EventHandler(priority = EventPriority.MONITOR)
   @Override
   public void onPlayerJoin(PlayerJoinEvent event) {
      super.onPlayerJoin(event);
      this.snapshots.put(event.getPlayer().getUniqueId(), new RotationSnapshot(event.getPlayer()));
   }

   @EventHandler(priority = EventPriority.MONITOR)
   @Override
   public void onPlayerQuit(PlayerQuitEvent event) {
      super.onPlayerQuit(event);
      this.snapshots.remove(event.getPlayer().getUniqueId());
   }

   @EventHandler(priority = EventPriority.MONITOR)
   @Override
   public void onPlayerKick(PlayerKickEvent event) {
      super.onPlayerKick(event);
      this.snapshots.remove(event.getPlayer().getUniqueId());
   }

   static ExpiringMap getRecentAttackPlayers(AimbotHeuristic aimbotHeuristic) {
      return aimbotHeuristic.recentAttackPlayers;
   }

   static Map getPreAttackYawValues(AimbotHeuristic aimbotHeuristic) {
      return aimbotHeuristic.preAttackYawValues;
   }

   static Map getLastYawValues(AimbotHeuristic aimbotHeuristic) {
      return aimbotHeuristic.lastYawValues;
   }


   private static class AimbotPacketListener extends PacketAdapter {
      final AimbotHeuristic heuristic;

      AimbotPacketListener(AimbotHeuristic aimbotHeuristic, Plugin plugin, PacketType... packetTypeArray) {
         super(plugin, packetTypeArray);
         this.heuristic = aimbotHeuristic;
      }

      public void onPacketReceiving(PacketEvent event) {
         try {
            if (event.getPacketType() == Client.USE_ENTITY) {
               WrapperPlayClientUseEntity wrapperPlayClientUseEntity = new WrapperPlayClientUseEntity(event.getPacket());
               if (wrapperPlayClientUseEntity.getType() == EntityUseAction.ATTACK) {
                  ExpiringMap expiringMap = AimbotHeuristic.getRecentAttackPlayers(this.heuristic);
                  UUID uuid = event.getPlayer().getUniqueId();
                  boolean flag = false;
                  expiringMap.put(uuid, flag);
               }
            } else if (AimbotHeuristic.getRecentAttackPlayers(this.heuristic).containsKey(event.getPlayer().getUniqueId())) {
               if (event.getPacketType() != Client.POSITION && event.getPacketType() != Client.FLYING) {
                  AimbotHeuristic.getPreAttackYawValues(this.heuristic).put(event.getPlayer().getUniqueId(), event.getPacket().getFloat().read(0));
                  AimbotHeuristic.getRecentAttackPlayers(this.heuristic).remove(event.getPlayer().getUniqueId());
               } else {
                  AimbotHeuristic.getRecentAttackPlayers(this.heuristic).remove(event.getPlayer().getUniqueId());
               }
            } else if (AimbotHeuristic.getPreAttackYawValues(this.heuristic).containsKey(event.getPlayer().getUniqueId())) {
               if (event.getPacketType() != Client.POSITION && event.getPacketType() != Client.FLYING) {
                  float f = (Float)AimbotHeuristic.getLastYawValues(this.heuristic).get(event.getPlayer().getUniqueId());
                  float f2 = (Float)event.getPacket().getFloat().read(0);
                  int value = (int)Math.abs((double)f2 - f);
                  int value2 = (int)Math.abs((double)f2 - ((Float)AimbotHeuristic.getPreAttackYawValues(this.heuristic).get(event.getPlayer().getUniqueId())).floatValue());
                  if (value < value2 - 5) {
                     this.heuristic.setHeuristicScore(event.getPlayer(), this.heuristic.getHeuristicScore(event.getPlayer()) + 91);
                  } else {
                     this.heuristic.setHeuristicScore(event.getPlayer(), this.heuristic.getHeuristicScore(event.getPlayer()) - 20);
                  }
               } else {
                  this.heuristic.setHeuristicScore(event.getPlayer(), this.heuristic.getHeuristicScore(event.getPlayer()) + 23);
               }

               AimbotHeuristic.getPreAttackYawValues(this.heuristic).remove(event.getPlayer().getUniqueId());
            } else if (event.getPacketType() == Client.LOOK || event.getPacketType() == Client.POSITION_LOOK) {
               float f3 = (Float)event.getPacket().getFloat().read(0);
               AimbotHeuristic.getLastYawValues(this.heuristic).put(event.getPlayer().getUniqueId(), f3);
            }
         } catch (NullPointerException ex) {
         }
      }
   }

   private static class DecayScoreTask implements Runnable {
      final AimbotHeuristic heuristic;

      DecayScoreTask(AimbotHeuristic aimbotHeuristic) {
         this.heuristic = aimbotHeuristic;
      }

      @Override
      public void run() {
         for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            this.heuristic.setHeuristicScore(player, (int)(this.heuristic.getHeuristicScore(player) * 0.86));
         }
      }
   }

   private static class RotationSnapshot {
      public double previousYaw;
      public double previousPitch;
      public long lastUpdateTime;
      public Location lastLocation;
      public Entity lastTarget;

      public RotationSnapshot(Player player) {
         this.previousYaw = player.getLocation().getYaw();
         this.previousPitch = player.getLocation().getPitch();
         this.lastUpdateTime = System.currentTimeMillis();
         this.lastLocation = null;
         this.lastTarget = null;
      }
   }
}
