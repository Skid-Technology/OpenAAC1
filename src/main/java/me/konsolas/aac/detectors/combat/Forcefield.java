package me.konsolas.aac.detectors.combat;

import com.comphenix.protocol.PacketType.Play.Client;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers.EntityUseAction;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import me.konsolas.aac.AAC;
import me.konsolas.aac.api.HackType;
import me.konsolas.aac.detectors.Detector;
import me.konsolas.aac.internal.packetwrapper.WrapperPlayClientUseEntity;
import me.konsolas.aac.internal.packetwrapper.WrapperPlayServerEntityStatus;
import me.konsolas.aac.util.ForcefieldDecoyManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

public final class Forcefield extends Detector {
   private static boolean sendDamagedAnimation = true;
   private static boolean useSidePlayers = false;
   private Map<UUID, ForcefieldDecoyManager> decoyManagers = new HashMap<>();
   private ConcurrentMap<UUID, Integer> hitCounts = new ConcurrentHashMap<>();
   private ConcurrentMap<UUID, Integer> missCounts = new ConcurrentHashMap<>();
   private ConcurrentMap<UUID, Long> lastAttackTimes = new ConcurrentHashMap<>();

   public Forcefield(AAC plugin) {
      super(plugin);
      DecoyHitPacketListener decoyHitPacketListener = new DecoyHitPacketListener(this, this.plugin, ListenerPriority.LOWEST, WrapperPlayClientUseEntity.TYPE, Client.ARM_ANIMATION);
      this.packetListeners.add(decoyHitPacketListener);
      ProtocolLibrary.getProtocolManager().addPacketListener(decoyHitPacketListener);
      this.scheduledTaskIds.add(Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, new ViolationCheckTask(this), 35L, 36L));
      this.scheduledTaskIds.add(Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, new SpawnCycleTask(this), 30L, 602L));
      ForcefieldDecoyManager.loadConfig(this.plugin);
      sendDamagedAnimation = this.plugin.getConfig().getBoolean("forcefield.magic.send_damaged_animation");
      useSidePlayers = this.plugin.getConfig().getBoolean("forcefield.use_side_players");
   }

   public ForcefieldDecoyManager removeDecoyManager(UUID uuid) {
      return this.decoyManagers.containsKey(uuid) ? this.decoyManagers.remove(uuid) : null;
   }

   @Override
   public void destroy() {
      super.destroy();

      for (Player player : Bukkit.getServer().getOnlinePlayers()) {
         ForcefieldDecoyManager forcefieldDecoyManager = this.removeDecoyManager(player.getUniqueId());
         if (forcefieldDecoyManager != null) {
            forcefieldDecoyManager.destroy();
         }
      }
   }

   private void spawnDecoysForPlayer(Player player) {
      ForcefieldDecoyManager forcefieldDecoyManager = new ForcefieldDecoyManager(this.plugin, player, this);
      this.decoyManagers.put(player.getUniqueId(), forcefieldDecoyManager);
      forcefieldDecoyManager.spawn();
   }

   @EventHandler
   public void onForcefieldPlayerQuit(PlayerQuitEvent event) {
      super.onPlayerQuit(event);
      ForcefieldDecoyManager forcefieldDecoyManager = this.removeDecoyManager(event.getPlayer().getUniqueId());
      if (forcefieldDecoyManager != null) {
         forcefieldDecoyManager.destroy();
      }
   }

   @EventHandler
   public void onForcefieldPlayerKick(PlayerKickEvent event) {
      super.onPlayerKick(event);
      ForcefieldDecoyManager forcefieldDecoyManager = this.removeDecoyManager(event.getPlayer().getUniqueId());
      if (forcefieldDecoyManager != null) {
         forcefieldDecoyManager.destroy();
      }
   }

   static Map getDecoyManagers(Forcefield forcefield) {
      return forcefield.decoyManagers;
   }

   static boolean isUseSidePlayers() {
      return useSidePlayers;
   }

   static ConcurrentMap getHitCounts(Forcefield forcefield) {
      return forcefield.hitCounts;
   }

   static ConcurrentMap getMissCounts(Forcefield forcefield) {
      return forcefield.missCounts;
   }

   static boolean isSendDamagedAnimation() {
      return sendDamagedAnimation;
   }

   static ConcurrentMap getLastAttackTimes(Forcefield forcefield) {
      return forcefield.lastAttackTimes;
   }


   static void spawnDecoysForPlayer(Forcefield forcefield, Player player) {
      forcefield.spawnDecoysForPlayer(player);
   }

   private static class DecoyHitPacketListener extends PacketAdapter {
      final Forcefield forcefield;

      DecoyHitPacketListener(Forcefield forcefield, Plugin plugin, ListenerPriority listenerPriority, PacketType... packetTypeArray) {
         super(plugin, listenerPriority, packetTypeArray);
         this.forcefield = forcefield;
      }

      public void onPacketReceiving(PacketEvent event) {
         if (event.getPacketType() == WrapperPlayClientUseEntity.TYPE) {
            WrapperPlayClientUseEntity wrapperPlayClientUseEntity = new WrapperPlayClientUseEntity(event.getPacket());
            int value = wrapperPlayClientUseEntity.getTargetID();
            if (Forcefield.getDecoyManagers(this.forcefield).containsKey(event.getPlayer().getUniqueId()) && wrapperPlayClientUseEntity.getType().equals(EntityUseAction.ATTACK)) {
               if (Forcefield.isUseSidePlayers() && ((ForcefieldDecoyManager)Forcefield.getDecoyManagers(this.forcefield).get(event.getPlayer().getUniqueId())).isSideEntityId(value)) {
                  Forcefield.getHitCounts(this.forcefield).putIfAbsent(event.getPlayer().getUniqueId(), 0);
                  Forcefield.getHitCounts(this.forcefield).put(event.getPlayer().getUniqueId(), (Integer)Forcefield.getHitCounts(this.forcefield).get(event.getPlayer().getUniqueId()) + 1);
                  Forcefield.getMissCounts(this.forcefield).putIfAbsent(event.getPlayer().getUniqueId(), 0);
                  Forcefield.getMissCounts(this.forcefield).put(event.getPlayer().getUniqueId(), (Integer)Forcefield.getMissCounts(this.forcefield).get(event.getPlayer().getUniqueId()) + 1);
                  event.setCancelled(true);
               } else if (((ForcefieldDecoyManager)Forcefield.getDecoyManagers(this.forcefield).get(event.getPlayer().getUniqueId())).isHeadEntityId(value)) {
                  Forcefield.getHitCounts(this.forcefield).putIfAbsent(event.getPlayer().getUniqueId(), 0);
                  Forcefield.getHitCounts(this.forcefield).put(event.getPlayer().getUniqueId(), (Integer)Forcefield.getHitCounts(this.forcefield).get(event.getPlayer().getUniqueId()) + 2);
                  Forcefield.getMissCounts(this.forcefield).putIfAbsent(event.getPlayer().getUniqueId(), 0);
                  Forcefield.getMissCounts(this.forcefield).put(event.getPlayer().getUniqueId(), (Integer)Forcefield.getMissCounts(this.forcefield).get(event.getPlayer().getUniqueId()) + 1);
                  event.setCancelled(true);
                  if (Forcefield.isSendDamagedAnimation()) {
                     if (Forcefield.getLastAttackTimes(this.forcefield).containsKey(event.getPlayer().getUniqueId())) {
                        if (System.currentTimeMillis() - 500L > (Long)Forcefield.getLastAttackTimes(this.forcefield).get(event.getPlayer().getUniqueId())) {
                           WrapperPlayServerEntityStatus wrapperPlayServerEntityStatus = ForcefieldDecoyManager.createStatusPacket(value);
                           wrapperPlayServerEntityStatus.sendPacket(event.getPlayer());
                           Forcefield.getLastAttackTimes(this.forcefield).put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
                        }
                     } else {
                        Forcefield.getLastAttackTimes(this.forcefield).put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
                     }
                  }
               }
            }
         }
      }
   }

   private static class ViolationCheckTask implements Runnable {
      final Forcefield forcefield;

      ViolationCheckTask(Forcefield forcefield) {
         this.forcefield = forcefield;
      }

      @Override
      public void run() {
         for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            if (!this.forcefield.getPlugin().checkManager.isPlayerExempt(player) && !player.getGameMode().equals(GameMode.SPECTATOR)) {
               if (Forcefield.getHitCounts(this.forcefield).containsKey(player.getUniqueId())) {
                  int value = (Integer)Forcefield.getHitCounts(this.forcefield).get(player.getUniqueId());
                  if (value > 4) {
                     this.forcefield.recordViolation(player.getUniqueId(), (value - 2) * 2, HackType.FORCEFIELD, player.getName() + " failed Forcefield: hit " + Forcefield.getMissCounts(this.forcefield).get(player.getUniqueId()) + " players when tested");
                  }
               }

               Forcefield.getHitCounts(this.forcefield).put(player.getUniqueId(), 0);
               Forcefield.getMissCounts(this.forcefield).put(player.getUniqueId(), 0);
               this.forcefield.decreaseViolationLevel(player.getUniqueId(), 2);
            }
         }
      }
   }

   private static class SpawnCycleTask implements Runnable {
      final Forcefield forcefield;

      SpawnCycleTask(Forcefield forcefield) {
         this.forcefield = forcefield;
      }

      @Override
      public void run() {
         for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            if (!this.forcefield.getPlugin().checkManager.isPlayerExempt(player) && !player.getGameMode().equals(GameMode.SPECTATOR)) {
               Forcefield.spawnDecoysForPlayer(this.forcefield, player);
            }
         }
      }
   }
}
