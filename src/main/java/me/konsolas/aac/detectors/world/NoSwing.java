package me.konsolas.aac.detectors.world;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers.EntityUseAction;
import me.konsolas.aac.AAC;
import me.konsolas.aac.api.HackType;
import me.konsolas.aac.detectors.Detector;
import me.konsolas.aac.internal.packetwrapper.WrapperPlayClientUseEntity;
import me.konsolas.aac.util.Ping;
import me.konsolas.aac.util.general.NumberUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.Plugin;

public final class NoSwing extends Detector {

   public NoSwing(AAC plugin) {
      super(plugin);
      AttackSwingPacketListener attackSwingPacketListener = new AttackSwingPacketListener(this, this.plugin, WrapperPlayClientUseEntity.TYPE);
      this.packetListeners.add(attackSwingPacketListener);
      ProtocolLibrary.getProtocolManager().addPacketListener(attackSwingPacketListener);
   }

   @EventHandler
   public void onBlockBreak(BlockBreakEvent event) {
      if (event.getPlayer().getGameMode() != GameMode.CREATIVE && !this.plugin.checkManager.isPlayerExempt(event.getPlayer())) {
         long now = System.currentTimeMillis();
         Player player = event.getPlayer();
         AAC plugin = this.plugin;
         this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new VerifyBreakSwingTask(this, plugin, player, now), 5L);
      }
   }


   private static class VerifyAttackSwingTask implements Runnable {
      final AAC plugin;
      final Player player;
      final long timestamp;
      final AttackSwingPacketListener listener;

      VerifyAttackSwingTask(AttackSwingPacketListener attackSwingPacketListener, AAC plugin, Player player, long ticks) {
         this.listener = attackSwingPacketListener;
         this.plugin = plugin;
         this.player = player;
         this.timestamp = ticks;
      }

      @Override
      public void run() {
         this.plugin
            .verboseLogger
            .logPlayerDebug(this.player, "(NoSwing) PlayClientUseEntity lastAnimated[e:" + this.timestamp + ",r:" + this.plugin.playerState.getLastArmSwingTime(this.player) + "]");

         try {
            if (!NumberUtil.isWithinRange(this.timestamp, this.plugin.playerState.getLastArmSwingTime(this.player), 300 + Ping.getPing(this.player))) {
               this.listener.noSwing.recordViolation(this.player.getUniqueId(), 1, HackType.NOSWING, this.player.getName() + " failed NoSwing: didn't swing arm (attack)");
            } else {
               this.listener.noSwing.decreaseViolationLevel(this.player.getUniqueId(), 2);
            }
         } catch (NullPointerException ex) {
         }
      }
   }

   private static class AttackSwingPacketListener extends PacketAdapter {
      final NoSwing noSwing;

      AttackSwingPacketListener(NoSwing noSwing, Plugin plugin, PacketType... packetTypeArray) {
         super(plugin, packetTypeArray);
         this.noSwing = noSwing;
      }

      public void onPacketReceiving(PacketEvent event) {
         if (!((AAC)this.plugin).checkManager.isPlayerExempt(event.getPlayer())) {
            if (event.getPacketType() != WrapperPlayClientUseEntity.TYPE || new WrapperPlayClientUseEntity(event.getPacket()).getType() == EntityUseAction.ATTACK) {
               long now = System.currentTimeMillis();
               Player player = event.getPlayer();
               AAC plugin = (AAC)this.plugin;
               Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new VerifyAttackSwingTask(this, plugin, player, now), 5L);
            }
         }
      }
   }

   private static class VerifyBreakSwingTask implements Runnable {
      final AAC plugin;
      final Player player;
      final long timestamp;
      final NoSwing noSwing;

      VerifyBreakSwingTask(NoSwing noSwing, AAC plugin, Player player, long ticks) {
         this.noSwing = noSwing;
         this.plugin = plugin;
         this.player = player;
         this.timestamp = ticks;
      }

      @Override
      public void run() {
         this.plugin
            .verboseLogger
            .logPlayerDebug(this.player, "(NoSwing) BlockBreakEvent lastAnimated[e:" + this.timestamp + ",r:" + this.plugin.playerState.getLastArmSwingTime(this.player) + "]");

         try {
            if (!NumberUtil.isWithinRange(this.timestamp, this.plugin.playerState.getLastArmSwingTime(this.player), 299 + Ping.getPing(this.player))) {
               this.noSwing.recordViolation(this.player.getUniqueId(), 1, HackType.NOSWING, this.player.getName() + " failed NoSwing: didn't swing arm (block break)");
            } else {
               this.noSwing.decreaseViolationLevel(this.player.getUniqueId(), 2);
            }
         } catch (NullPointerException ex) {
         }
      }
   }
}
