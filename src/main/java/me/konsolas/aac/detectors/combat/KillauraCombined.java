package me.konsolas.aac.detectors.combat;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import me.konsolas.aac.AAC;
import me.konsolas.aac.api.HackType;
import me.konsolas.aac.detectors.DamageListener;
import me.konsolas.aac.detectors.Detector;
import me.konsolas.aac.heuristics.combat.AccuracyHeuristic;
import me.konsolas.aac.heuristics.combat.AimbotHeuristic;
import me.konsolas.aac.heuristics.combat.ReachHeuristic;
import me.konsolas.aac.internal.packetwrapper.WrapperPlayClientUseEntity;
import me.konsolas.aac.util.CombatDecoyPair;
import me.konsolas.aac.util.EntityTargetCategories;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

public final class KillauraCombined extends Detector implements DamageListener {
   private int checkChance = 100;
   private int checkDelay = 8;
   private double minDist = 2.7;
   private int npcMultiplier = 6;
   private int suspendedCombatTime = 12;
   private boolean randomType = true;
   private Map<UUID, CombatDecoyPair> activeDecoys = new HashMap<>();
   private Set<UUID> suspendedPlayers = new HashSet<>();
   AimbotHeuristic aimbotHeuristic;
   ReachHeuristic reachHeuristic;
   AccuracyHeuristic accuracyHeuristic;
   private int aimbot = 80;
   private int reach = 80;
   private int accuracy = 80;

   public KillauraCombined(AAC plugin) {
      super(plugin);
      this.checkChance = this.plugin.getConfig().getInt("killaura.npc.check_chance", this.checkChance);
      this.checkDelay = this.plugin.getConfig().getInt("killaura.npc.check_delay", this.checkDelay);
      this.minDist = this.plugin.getConfig().getDouble("killaura.npc.min_dist", this.minDist);
      this.npcMultiplier = this.plugin.getConfig().getInt("killaura.npc.npc_multiplier", this.npcMultiplier);
      this.suspendedCombatTime = this.plugin.getConfig().getInt("killaura.npc.suspended_combat_time", this.suspendedCombatTime);
      this.randomType = this.plugin.getConfig().getBoolean("killaura.random_type", this.randomType);
      this.aimbot = this.plugin.getConfig().getInt("killaura.aimbot", this.aimbot);
      this.reach = this.plugin.getConfig().getInt("killaura.reach", this.reach);
      this.accuracy = this.plugin.getConfig().getInt("killaura.accuracy", this.accuracy);
      this.scheduledTaskIds.add(Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, new ResetCombatTargetsTask(this), 100L, 79L));
      CombatDecoyPair.loadConfig(this.plugin);
      DecoyHitPacketListener decoyHitPacketListener = new DecoyHitPacketListener(this, this.plugin, WrapperPlayClientUseEntity.TYPE);
      this.packetListeners.add(decoyHitPacketListener);
      ProtocolLibrary.getProtocolManager().addPacketListener(decoyHitPacketListener);
      this.aimbotHeuristic = new AimbotHeuristic(this.plugin);
      this.reachHeuristic = new ReachHeuristic(this.plugin);
      this.accuracyHeuristic = new AccuracyHeuristic(this.plugin);
      Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, new DecayViolationsTask(this), 31L, 29L);
   }

   @Override
   public boolean shouldProcessDamage(EntityDamageByEntityEvent event) {
      return event.getDamager().getType() == EntityType.PLAYER
         && EntityTargetCategories.isKillauraTarget(event.getEntity().getType())
         && !this.activeDecoys.containsKey(event.getDamager().getUniqueId())
         && !(event.getDamager().getLocation().distance(event.getEntity().getLocation()) < this.minDist);
   }

   @Override
   public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
      if (AAC.random.nextInt(100) < this.checkChance) {
         Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new SpawnDecoyTask(this, event), this.checkDelay);
      }
   }

   private EntityType pickRandomEntityType() {
      int value = AAC.random.nextInt(6);
      switch (value) {
         case 0:
            return EntityType.PLAYER;
         case 1:
            return EntityType.VILLAGER;
         case 2:
            return EntityType.ZOMBIE;
         case 3:
            return EntityType.SKELETON;
         case 4:
            return EntityType.PIG_ZOMBIE;
         case 5:
            return EntityType.WITCH;
         default:
            throw new AssertionError();
      }
   }

   @Override
   public void onPlayerQuit(PlayerQuitEvent event) {
      super.onPlayerQuit(event);
      if (this.activeDecoys.containsKey(event.getPlayer().getUniqueId())) {
         this.activeDecoys.get(event.getPlayer().getUniqueId()).destroy();
         this.activeDecoys.remove(event.getPlayer().getUniqueId());
      }
   }

   @Override
   public void onPlayerKick(PlayerKickEvent event) {
      super.onPlayerKick(event);
      if (this.activeDecoys.containsKey(event.getPlayer().getUniqueId())) {
         this.activeDecoys.get(event.getPlayer().getUniqueId()).destroy();
         this.activeDecoys.remove(event.getPlayer().getUniqueId());
      }
   }

   @Override
   public void destroy() {
      super.destroy();
      this.aimbotHeuristic.destroy();
   }


   static Set getSuspendedPlayers(KillauraCombined killauraCombined) {
      return killauraCombined.suspendedPlayers;
   }


   static int getAimbotThreshold(KillauraCombined killauraCombined) {
      return killauraCombined.aimbot;
   }


   static int getReachThreshold(KillauraCombined killauraCombined) {
      return killauraCombined.reach;
   }


   static int getAccuracyThreshold(KillauraCombined killauraCombined) {
      return killauraCombined.accuracy;
   }


   static int getNpcMultiplier(KillauraCombined killauraCombined) {
      return killauraCombined.npcMultiplier;
   }


   static int getSuspendedCombatTime(KillauraCombined killauraCombined) {
      return killauraCombined.suspendedCombatTime;
   }


   static Map getActiveDecoys(KillauraCombined killauraCombined) {
      return killauraCombined.activeDecoys;
   }

   static boolean isRandomType(KillauraCombined killauraCombined) {
      return killauraCombined.randomType;
   }

   static EntityType pickRandomEntityType(KillauraCombined killauraCombined) {
      return killauraCombined.pickRandomEntityType();
   }

   private static class ResetCombatTargetsTask implements Runnable {
      final KillauraCombined killaura;

      ResetCombatTargetsTask(KillauraCombined killauraCombined) {
         this.killaura = killauraCombined;
      }

      @Override
      public void run() {
         for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            this.killaura.decreaseViolationLevel(player.getUniqueId(), 1);
         }
      }
   }

   private static class DecoyHitPacketListener extends PacketAdapter {
      final KillauraCombined killaura;

      DecoyHitPacketListener(KillauraCombined killauraCombined, Plugin plugin, PacketType... packetTypeArray) {
         super(plugin, packetTypeArray);
         this.killaura = killauraCombined;
      }

      public void onPacketReceiving(PacketEvent event) {
         if (event.getPacketType() == WrapperPlayClientUseEntity.TYPE && KillauraCombined.getSuspendedPlayers(this.killaura).contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
         }
      }
   }

   private static class DecayViolationsTask implements Runnable {
      final KillauraCombined killaura;

      DecayViolationsTask(KillauraCombined killauraCombined) {
         this.killaura = killauraCombined;
      }

      @Override
      public void run() {
         for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            if (!this.killaura.getPlugin().checkManager.isPlayerExempt(player)) {
               if (this.killaura.aimbotHeuristic.getDetectionChance(player) > KillauraCombined.getAimbotThreshold(this.killaura)) {
                  this.killaura.recordViolation(player.getUniqueId(), this.killaura.aimbotHeuristic.getDetectionChance(player) / 5 + 2, HackType.KILLAURA, player.getName() + " failed KillauraCombined: (AimbotHeuristic) Chance of hacking = " + this.killaura.aimbotHeuristic.getDetectionChance(player));
               }

               if (this.killaura.reachHeuristic.getDetectionChance(player) > KillauraCombined.getReachThreshold(this.killaura)) {
                  this.killaura.recordViolation(player.getUniqueId(), this.killaura.reachHeuristic.getDetectionChance(player) / 5 + 2, HackType.KILLAURA, player.getName() + " failed KillauraCombined: (ReachHeuristic) Chance of hacking = " + this.killaura.reachHeuristic.getDetectionChance(player));
               }

               if (this.killaura.accuracyHeuristic.getDetectionChance(player) > KillauraCombined.getAccuracyThreshold(this.killaura)) {
                  this.killaura.recordViolation(player.getUniqueId(), this.killaura.accuracyHeuristic.getDetectionChance(player) / 5 + 2, HackType.KILLAURA, player.getName() + " failed KillauraCombined: (AccuracyHeuristic) Chance of hacking = " + this.killaura.accuracyHeuristic.getDetectionChance(player));
               }
            }
         }
      }
   }

   private static class ClearSuspendedCombatTask implements Runnable {
      final DecoyResultHandler handler;

      ClearSuspendedCombatTask(DecoyResultHandler decoyResultHandler) {
         this.handler = decoyResultHandler;
      }

      @Override
      public void run() {
         KillauraCombined.getSuspendedPlayers(this.handler.spawnTask.killaura).remove(this.handler.spawnTask.event.getDamager().getUniqueId());
      }
   }

   private static class DecoyResultHandler implements CombatDecoyPair.HitListener {
      final SpawnDecoyTask spawnTask;

      DecoyResultHandler(SpawnDecoyTask spawnDecoyTask) {
         this.spawnTask = spawnDecoyTask;
      }

      @Override
      public void onDecoyComplete(int i, int i2) {
         if (i2 > 0) {
            this.spawnTask.killaura.recordViolation(this.spawnTask.event.getDamager().getUniqueId(), KillauraCombined.getNpcMultiplier(this.spawnTask.killaura), HackType.KILLAURA, this.spawnTask.event.getDamager().getName() + " failed Killaura: attacked through another player");
            KillauraCombined.getSuspendedPlayers(this.spawnTask.killaura).add(this.spawnTask.event.getDamager().getUniqueId());
            Bukkit.getServer()
               .getScheduler()
               .scheduleSyncDelayedTask(this.spawnTask.killaura.getPlugin(), new ClearSuspendedCombatTask(this), KillauraCombined.getSuspendedCombatTime(this.spawnTask.killaura));
         } else {
            this.spawnTask.killaura.decreaseViolationLevel(this.spawnTask.event.getDamager().getUniqueId(), 1);
         }

         KillauraCombined.getActiveDecoys(this.spawnTask.killaura).remove(this.spawnTask.event.getDamager().getUniqueId());
      }
   }

   private static class SpawnDecoyTask implements Runnable {
      final EntityDamageByEntityEvent event;
      final KillauraCombined killaura;

      SpawnDecoyTask(KillauraCombined killauraCombined, EntityDamageByEntityEvent event) {
         this.killaura = killauraCombined;
         this.event = event;
      }

      @Override
      public void run() {
         if (((OfflinePlayer)this.event.getDamager()).isOnline()) {
            CombatDecoyPair combatDecoyPair = new CombatDecoyPair(this.killaura.getPlugin(), (Player)this.event.getDamager(), this.event.getEntity(), new DecoyResultHandler(this));
            KillauraCombined.getActiveDecoys(this.killaura).put(this.event.getDamager().getUniqueId(), combatDecoyPair);
            if (KillauraCombined.isRandomType(this.killaura)) {
               combatDecoyPair.spawn(KillauraCombined.pickRandomEntityType(this.killaura));
            } else if (EntityTargetCategories.isPrimaryCombatTarget(this.event.getEntity().getType())) {
               combatDecoyPair.spawn(this.event.getEntity().getType());
            } else {
               combatDecoyPair.spawn(EntityType.PLAYER);
            }
         }
      }
   }
}
