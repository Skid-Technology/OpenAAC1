package me.konsolas.aac.detectors.combat;

import com.comphenix.protocol.PacketType.Play.Client;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers.EntityUseAction;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Map;
import java.util.UUID;
import me.konsolas.aac.AAC;
import me.konsolas.aac.api.HackType;
import me.konsolas.aac.detectors.Detector;
import me.konsolas.aac.internal.packetwrapper.WrapperPlayClientUseEntity;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

public final class FightSpeed extends Detector {
   private int maxHitsPerSecond = 12;
   private int maxInteractPerSecond = 25;
   private boolean consistentCheck = true;
   private Map<UUID, Integer> hitCounts = new HashMap<>();
   private Map<UUID, Integer> interactCounts = new HashMap<>();
   private Map<UUID, Integer> swingCounts = new HashMap<>();
   private Map<UUID, Integer> lastSwingCounts = new HashMap<>();
   private Map<UUID, Integer> previousSwingCounts = new HashMap<>();

   public FightSpeed(AAC plugin) {
      super(plugin);
      this.maxHitsPerSecond = this.plugin.getConfig().getInt("fightspeed.max_hits_per_second", this.maxHitsPerSecond);
      this.maxInteractPerSecond = this.plugin.getConfig().getInt("fightspeed.max_interact_per_second", this.maxInteractPerSecond);
      this.consistentCheck = this.plugin.getConfig().getBoolean("fightspeed.consistent_check", this.consistentCheck);
      AttackPacketListener attackPacketListener = new AttackPacketListener(this, this.plugin, Client.USE_ENTITY, Client.ARM_ANIMATION);
      this.packetListeners.add(attackPacketListener);
      ProtocolLibrary.getProtocolManager().addPacketListener(attackPacketListener);
      Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, new DecayViolationsTask(this), 10L, 59L);
   }

   @Override
   public void setCurrentTick(long ticks) {
      super.setCurrentTick(ticks);
      if (ticks % 20L == 0L) {
         for (Entry entry : this.hitCounts.entrySet()) {
            Player player = Bukkit.getPlayer((UUID)entry.getKey());
            if ((Integer)entry.getValue() > this.maxHitsPerSecond) {
               if ((Integer)entry.getValue() != 20 && (Integer)entry.getValue() != 21 && (Integer)entry.getValue() != 19) {
                  this.recordViolation(
                     (UUID)entry.getKey(),
                     (Integer)entry.getValue() - this.maxHitsPerSecond + 3,
                     HackType.FIGHTSPEED,
                     player.getName() + " failed FightSpeed: reached a CPS of " + entry.getValue()
                  );
               } else {
                  this.recordViolation(
                     (UUID)entry.getKey(),
                     ((Integer)entry.getValue() - this.maxHitsPerSecond + 3) * 2,
                     HackType.FIGHTSPEED,
                     player.getName() + " failed FightSpeed: reached a CPS of " + entry.getValue()
                  );
               }
            }

            this.hitCounts.put((UUID)entry.getKey(), 0);
         }

         for (Entry entry2 : this.interactCounts.entrySet()) {
            Player player2 = Bukkit.getPlayer((UUID)entry2.getKey());
            if ((Integer)entry2.getValue() > this.maxInteractPerSecond) {
               this.recordViolation(
                  (UUID)entry2.getKey(),
                  (Integer)entry2.getValue() - this.maxHitsPerSecond + 3,
                  HackType.FIGHTSPEED,
                  player2.getName() + " failed FightSpeed: interacting " + entry2.getValue() + " times per second"
               );
            }

            this.interactCounts.put((UUID)entry2.getKey(), 0);
         }

         for (Entry entry3 : this.swingCounts.entrySet()) {
            Player player3 = Bukkit.getPlayer((UUID)entry3.getKey());
            if (this.lastSwingCounts.get(entry3.getKey()) != null && this.previousSwingCounts.get(entry3.getKey()) != null) {
               int value = this.lastSwingCounts.get(entry3.getKey());
               int value2 = this.previousSwingCounts.get(entry3.getKey());
               if ((Integer)entry3.getValue() < 9) {
                  this.previousSwingCounts.put((UUID)entry3.getKey(), this.lastSwingCounts.get(entry3.getKey()));
                  this.lastSwingCounts.put((UUID)entry3.getKey(), (Integer)entry3.getValue());
                  this.swingCounts.put((UUID)entry3.getKey(), 0);
                  continue;
               }

               if (this.consistentCheck && value == (Integer)entry3.getValue() && (Integer)entry3.getValue() == value2) {
                  if (value != 10 && value != 20) {
                     this.recordViolation(
                        (UUID)entry3.getKey(),
                        3,
                        HackType.FIGHTSPEED,
                        player3.getName() + " failed FightSpeed: constantly clicking at a CPS of " + entry3.getValue()
                     );
                  } else {
                     this.recordViolation(
                        (UUID)entry3.getKey(),
                        7,
                        HackType.FIGHTSPEED,
                        player3.getName() + " failed FightSpeed: constantly clicking at a CPS of " + entry3.getValue()
                     );
                  }
               }
            }

            this.previousSwingCounts.put((UUID)entry3.getKey(), this.lastSwingCounts.get(entry3.getKey()));
            this.lastSwingCounts.put((UUID)entry3.getKey(), (Integer)entry3.getValue());
            this.swingCounts.put((UUID)entry3.getKey(), 0);
         }
      }
   }

   @Override
   public void onPlayerJoin(PlayerJoinEvent event) {
      super.onPlayerJoin(event);
      this.hitCounts.put(event.getPlayer().getUniqueId(), 0);
   }

   @EventHandler(priority = EventPriority.MONITOR)
   @Override
   public void onPlayerQuit(PlayerQuitEvent event) {
      super.onPlayerQuit(event);
      this.hitCounts.remove(event.getPlayer().getUniqueId());
   }

   @EventHandler(priority = EventPriority.MONITOR)
   @Override
   public void onPlayerKick(PlayerKickEvent event) {
      super.onPlayerKick(event);
      this.hitCounts.remove(event.getPlayer().getUniqueId());
   }

   static Map getHitCounts(FightSpeed fightSpeed) {
      return fightSpeed.hitCounts;
   }

   static Map getInteractCounts(FightSpeed fightSpeed) {
      return fightSpeed.interactCounts;
   }

   static Map getSwingCounts(FightSpeed fightSpeed) {
      return fightSpeed.swingCounts;
   }


   private static class AttackPacketListener extends PacketAdapter {
      final FightSpeed fightSpeed;

      AttackPacketListener(FightSpeed fightSpeed, Plugin plugin, PacketType... packetTypeArray) {
         super(plugin, packetTypeArray);
         this.fightSpeed = fightSpeed;
      }

      public void onPacketReceiving(PacketEvent event) {
         if (!((AAC)this.plugin).checkManager.isPlayerExempt(event.getPlayer())) {
            if (event.getPacketType() == Client.USE_ENTITY) {
               WrapperPlayClientUseEntity wrapperPlayClientUseEntity = new WrapperPlayClientUseEntity(event.getPacket());
               if (wrapperPlayClientUseEntity.getType() == EntityUseAction.ATTACK) {
                  if (!FightSpeed.getHitCounts(this.fightSpeed).containsKey(event.getPlayer().getUniqueId())) {
                     FightSpeed.getHitCounts(this.fightSpeed).put(event.getPlayer().getUniqueId(), 0);
                  }

                  FightSpeed.getHitCounts(this.fightSpeed).put(event.getPlayer().getUniqueId(), (Integer)FightSpeed.getHitCounts(this.fightSpeed).get(event.getPlayer().getUniqueId()) + 1);
               } else {
                  if (!FightSpeed.getInteractCounts(this.fightSpeed).containsKey(event.getPlayer().getUniqueId())) {
                     FightSpeed.getInteractCounts(this.fightSpeed).put(event.getPlayer().getUniqueId(), 0);
                  }

                  FightSpeed.getInteractCounts(this.fightSpeed).put(event.getPlayer().getUniqueId(), (Integer)FightSpeed.getInteractCounts(this.fightSpeed).get(event.getPlayer().getUniqueId()) + 1);
               }
            } else if (((AAC)this.plugin).combatLogoutListener.isInCombat(event.getPlayer())) {
               if (!FightSpeed.getSwingCounts(this.fightSpeed).containsKey(event.getPlayer().getUniqueId())) {
                  FightSpeed.getSwingCounts(this.fightSpeed).put(event.getPlayer().getUniqueId(), 0);
               }

               FightSpeed.getSwingCounts(this.fightSpeed).put(event.getPlayer().getUniqueId(), (Integer)FightSpeed.getSwingCounts(this.fightSpeed).get(event.getPlayer().getUniqueId()) + 1);
            }
         }
      }
   }

   private static class DecayViolationsTask implements Runnable {
      final FightSpeed fightSpeed;

      DecayViolationsTask(FightSpeed fightSpeed) {
         this.fightSpeed = fightSpeed;
      }

      @Override
      public void run() {
         for (Player player : this.fightSpeed.getPlugin().getServer().getOnlinePlayers()) {
            this.fightSpeed.decreaseViolationLevel(player.getUniqueId(), 1);
         }
      }
   }
}
