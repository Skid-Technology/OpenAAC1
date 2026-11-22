package me.konsolas.aac;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import me.konsolas.aac.api.HackType;
import me.konsolas.aac.detectors.DamageListener;
import me.konsolas.aac.detectors.Detector;
import me.konsolas.aac.detectors.MoveListener;
import me.konsolas.aac.util.blocks.BlockCollisionChecks;
import me.konsolas.aac.util.blocks.RelativeBlockCache;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class CheckManager implements Listener {
   private Map<HackType, Detector> detectors = new HashMap<>();
   private List<HackType> disabledChecks = new ArrayList<>();
   private Set<UUID> cachedBypassPlayers = new HashSet<>();
   private Map<UUID, Long> nextBypassRefreshTimes = new HashMap<>();
   private AAC plugin;
   private long tickCounter = 0L;

   public CheckManager(AAC plugin) {
      this.plugin = plugin;

      for (HackType hackType : HackType.values()) {
         this.detectors.put(hackType, null);
      }

      Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, new TickCounterTask(this), 1L, 1L);
   }


   public boolean enableCheck(HackType hackType, boolean flag) {
      if (this.detectors.get(hackType) != null) {
         return false;
      }

      if (flag) {
         if (this.disabledChecks.contains(hackType)) {
            return false;
         }
      } else {
         this.disabledChecks.remove(hackType);
      }

      Class classVal;
      try {
         classVal = Class.forName("me.konsolas.aac.detectors." + hackType);
      } catch (ClassNotFoundException ex) {
         this.plugin.verboseLogger.logViolation("Failed to initialize " + hackType.getName() + " (class not found)");
         return false;
      }

      Constructor[] constructorArray = classVal.getDeclaredConstructors();
      Constructor constructor = null;

      for (Constructor constructor2 : constructorArray) {
         constructor = constructor2;
         if (constructor.getParameterTypes().length == 1) {
            break;
         }
      }

      if (constructor == null) {
         throw new RuntimeException("Could not get valid constuctor");
      }

      Detector detector;
      try {
         constructor.setAccessible(true);
         detector = (Detector)constructor.newInstance(this.plugin);
      } catch (Exception ex2) {
         this.plugin.verboseLogger.logViolation("Failed to initialize " + hackType.getName() + " (instance creation failed)");
         ex2.printStackTrace();
         return false;
      }

      this.detectors.put(hackType, detector);
      Bukkit.getServer().getPluginManager().registerEvents(detector, this.plugin);
      this.plugin.verboseLogger.logViolation("(CheckManager) Enabled check " + hackType);
      return true;
   }

   public boolean disableCheck(HackType hackType, boolean flag) {
      if (!this.isCheckEnabled(hackType)) {
         return false;
      }

      if (!flag) {
         this.disabledChecks.add(hackType);
      }

      this.detectors.get(hackType).destroy();
      HandlerList.unregisterAll(this.detectors.get(hackType));
      this.detectors.put(hackType, null);
      this.plugin.verboseLogger.logViolation("(CheckManager) Disabled check " + hackType);
      return true;
   }

   public void disableAllChecks() {
      for (HackType hackType : HackType.values()) {
         if (this.isCheckEnabled(hackType)) {
            this.plugin.getLogger().info("(CheckManager) Disabling check " + hackType);
            this.disableCheck(hackType, false);
         } else {
            this.plugin.getLogger().info("(CheckManager) Check " + hackType + " is not enabled");
         }
      }
   }

   public void enableConfiguredChecks() {
      for (HackType hackType : HackType.values()) {
         this.enableCheck(hackType, true);
      }
   }

   public Detector getDetector(HackType hackType) {
      return this.detectors.get(hackType);
   }

   public boolean isCheckEnabled(HackType hackType) {
      return this.detectors.get(hackType) != null;
   }

   @EventHandler(priority = EventPriority.MONITOR)
   public void onPlayerKick(PlayerKickEvent event) {
      for (HackType hackType : HackType.values()) {
         if (this.isCheckEnabled(hackType)) {
            this.getDetector(hackType).onPlayerKick(event);
         }
      }
   }

   @EventHandler(priority = EventPriority.MONITOR)
   public void onPlayerJoin(PlayerJoinEvent event) {
      for (HackType hackType : HackType.values()) {
         if (this.isCheckEnabled(hackType)) {
            this.getDetector(hackType).onPlayerJoin(event);
         }
      }
   }

   @EventHandler(priority = EventPriority.MONITOR)
   public void onPlayerQuit(PlayerQuitEvent event) {
      for (HackType hackType : HackType.values()) {
         if (this.isCheckEnabled(hackType)) {
            this.getDetector(hackType).onPlayerQuit(event);
         }
      }
   }

   @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
   public void onPlayerMove(PlayerMoveEvent event) {
      boolean flag = this.isPlayerExempt(event.getPlayer());
      RelativeBlockCache relativeBlockCache = new RelativeBlockCache(event.getFrom());
      RelativeBlockCache relativeBlockCache2 = new RelativeBlockCache(event.getTo());
      boolean flag2 = BlockCollisionChecks.isOnGround(event.getFrom(), relativeBlockCache);
      boolean flag3 = BlockCollisionChecks.isOnGround(event.getTo(), relativeBlockCache2);
      this.plugin.playerState.recordMoveState(event, flag2, flag3);

      for (HackType hackType : HackType.values()) {
         if (this.isCheckEnabled(hackType)) {
            Detector detector = this.getDetector(hackType);
            if (detector instanceof MoveListener) {
               MoveListener moveListener = (MoveListener)detector;
               if (moveListener.shouldProcessPlayerMove(event) && !flag) {
                  moveListener.onPlayerMove(event, flag2, flag3, relativeBlockCache, relativeBlockCache2);
               }
            }
         }
      }

      if (this.plugin.movementDispatcher.shouldProcessPlayerMove(event) && !flag) {
         this.plugin.movementDispatcher.onPlayerMove(event, flag2, flag3, relativeBlockCache, relativeBlockCache2);
      }
   }

   @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
   public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
      boolean flag = event.getDamager().hasPermission("AAC.bypass") || event.getEntity().hasPermission("AAC.bypass");

      for (HackType hackType : HackType.values()) {
         if (this.isCheckEnabled(hackType)) {
            Detector detector = this.getDetector(hackType);
            if (detector instanceof DamageListener) {
               DamageListener damageListener = (DamageListener)detector;
               if (damageListener.shouldProcessDamage(event) && !flag) {
                  damageListener.onEntityDamageByEntity(event);
               }
            }
         }
      }
   }

   public boolean isPlayerExempt(Player player) {
      if (!this.nextBypassRefreshTimes.containsKey(player.getUniqueId())) {
         this.nextBypassRefreshTimes.put(player.getUniqueId(), 0L);
      }

      boolean flag;
      if (this.cachedBypassPlayers.contains(player.getUniqueId())) {
         if (System.currentTimeMillis() < this.nextBypassRefreshTimes.get(player.getUniqueId())) {
            flag = true;
         } else {
            flag = player.hasPermission("AAC.bypass");
            if (flag) {
               this.cachedBypassPlayers.add(player.getUniqueId());
            } else {
               this.cachedBypassPlayers.remove(player.getUniqueId());
            }

            this.nextBypassRefreshTimes.put(player.getUniqueId(), System.currentTimeMillis() + AAC.random.nextInt(20000) + 20000L);
         }
      } else if (System.currentTimeMillis() < this.nextBypassRefreshTimes.get(player.getUniqueId())) {
         flag = false;
      } else {
         flag = player.hasPermission("AAC.bypass");
         if (flag) {
            this.cachedBypassPlayers.add(player.getUniqueId());
         } else {
            this.cachedBypassPlayers.remove(player.getUniqueId());
         }

         this.nextBypassRefreshTimes.put(player.getUniqueId(), System.currentTimeMillis() + AAC.random.nextInt(20000) + 20000L);
      }

      return flag;
   }

   public void clearBypassCache() {
      this.nextBypassRefreshTimes.clear();
      this.cachedBypassPlayers.clear();
   }

   static long incrementTickCounter(CheckManager checkManager) {
      return checkManager.tickCounter++;
   }

   static long getTickCounter(CheckManager checkManager) {
      return checkManager.tickCounter;
   }

   private static class TickCounterTask implements Runnable {
      final CheckManager checkManager;

      TickCounterTask(CheckManager checkManager) {
         this.checkManager = checkManager;
      }

      @Override
      public void run() {
         CheckManager.incrementTickCounter(this.checkManager);

         for (HackType hackType : HackType.values()) {
            if (this.checkManager.isCheckEnabled(hackType)) {
               this.checkManager.getDetector(hackType).setCurrentTick(CheckManager.getTickCounter(this.checkManager));
            }
         }
      }
   }
}
