package me.konsolas.aac.heuristics.combat;

import com.comphenix.protocol.PacketType.Play.Client;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.wrappers.EnumWrappers.EntityUseAction;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import me.konsolas.aac.AAC;
import me.konsolas.aac.heuristics.base.Heuristic;
import me.konsolas.aac.internal.packetwrapper.WrapperPlayClientUseEntity;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class AccuracyHeuristic extends Heuristic {
   private static final int SAMPLE_TICKS = 200;
   private PacketListener packetListener;
   private ExpiringMap<UUID, Boolean> recentLookChanges = ExpiringMap.builder().expiration(3L, TimeUnit.MILLISECONDS).expirationPolicy(ExpirationPolicy.CREATED).build();

   public AccuracyHeuristic(AAC plugin) {
      super(plugin);
      this.packetListener = new AccuracyPacketListener(this, this.plugin, Client.FLYING, Client.POSITION, Client.POSITION_LOOK, Client.USE_ENTITY, Client.LOOK);
      ProtocolLibrary.getProtocolManager().addPacketListener(this.packetListener);
   }

   private float wrapAngleDiff(float f, float f2) {
      float f3 = Math.abs(f - f2);
      float f4 = f3 % 360.0F;
      return f4 > 180.0F ? 360.0F - f4 : f4;
   }

   @Override
   public void destroy() {
      super.destroy();
      ProtocolLibrary.getProtocolManager().removePacketListener(this.packetListener);
   }

   @Override
   public int getDetectionChance(Player player) {
      int value = (int)(Math.pow(this.getHeuristicScore(player), 0.79) / 2.0);
      return value >= 100 ? 99 : value;
   }

   static ExpiringMap getRecentLookChanges(AccuracyHeuristic accuracyHeuristic) {
      return accuracyHeuristic.recentLookChanges;
   }


   private static class RecordMissTask implements Runnable {
      final PacketEvent event;
      final AccuracyPacketListener listener;

      RecordMissTask(AccuracyPacketListener accuracyPacketListener, PacketEvent event) {
         this.listener = accuracyPacketListener;
         this.event = event;
      }

      @Override
      public void run() {
         this.listener.heuristic.setHeuristicScore(this.event.getPlayer(), this.listener.heuristic.getHeuristicScore(this.event.getPlayer()) + 90);
      }
   }

   private static class RecordHitTask implements Runnable {
      final PacketEvent event;
      final AccuracyPacketListener listener;

      RecordHitTask(AccuracyPacketListener accuracyPacketListener, PacketEvent event) {
         this.listener = accuracyPacketListener;
         this.event = event;
      }

      @Override
      public void run() {
         this.listener.heuristic.setHeuristicScore(this.event.getPlayer(), (int)(this.listener.heuristic.getHeuristicScore(this.event.getPlayer()) * 0.75));
      }
   }

   private static class AccuracyPacketListener extends PacketAdapter {
      final AccuracyHeuristic heuristic;

      AccuracyPacketListener(AccuracyHeuristic accuracyHeuristic, Plugin plugin, PacketType... packetTypeArray) {
         super(plugin, packetTypeArray);
         this.heuristic = accuracyHeuristic;
      }

      public void onPacketReceiving(PacketEvent event) {
         if (event.getPacketType() == Client.USE_ENTITY) {
            WrapperPlayClientUseEntity wrapperPlayClientUseEntity = new WrapperPlayClientUseEntity(event.getPacket());
            if (wrapperPlayClientUseEntity.getType() == EntityUseAction.ATTACK) {
               if (AccuracyHeuristic.getRecentLookChanges(this.heuristic).containsKey(event.getPlayer().getUniqueId())) {
                  this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new RecordMissTask(this, event), 200L);
               } else {
                  this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new RecordHitTask(this, event), 200L);
               }
            }
         } else {
            ExpiringMap expiringMap = AccuracyHeuristic.getRecentLookChanges(this.heuristic);
            UUID uuid = event.getPlayer().getUniqueId();
            boolean flag = false;
            expiringMap.put(uuid, flag);
         }
      }
   }
}
