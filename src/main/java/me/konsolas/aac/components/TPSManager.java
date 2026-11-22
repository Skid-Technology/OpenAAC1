package me.konsolas.aac.components;

import java.util.HashSet;
import java.util.Set;
import me.konsolas.aac.AAC;
import me.konsolas.aac.api.HackType;
import me.konsolas.aac.util.TPS;
import org.bukkit.Bukkit;

public class TPSManager {
   private AAC plugin;
   private final Set<HackType> configuredChecks = new HashSet<>();

   public TPSManager(AAC plugin) {
      this.plugin = plugin;

      for (HackType hackType : HackType.values()) {
         if (this.plugin.getConfig().getBoolean(hackType.getName() + ".enabled")) {
            this.configuredChecks.add(hackType);
         }
      }

      Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, new TpsMonitorTask(this), 20L, 13L);
   }

   static Set<HackType> getConfiguredChecks(TPSManager tpsManager) {
      return tpsManager.configuredChecks;
   }

   static AAC getPlugin(TPSManager tpsManager) {
      return tpsManager.plugin;
   }

   private static class TpsMonitorTask implements Runnable {
      final TPSManager tpsManager;

      TpsMonitorTask(TPSManager tpsManager) {
         this.tpsManager = tpsManager;
      }

      @Override
      public void run() {
         double d = TPS.getTps();
         if (d > 18.5) {
            for (HackType hackType : TPSManager.getConfiguredChecks(this.tpsManager)) {
               TPSManager.getPlugin(this.tpsManager).checkManager.enableCheck(hackType, true);
            }
         } else {
            TPSManager.getPlugin(this.tpsManager).verboseLogger.logViolation("(TPSManager) TPS dropped to " + d);
            if (d < 17.5) {
               TPSManager.getPlugin(this.tpsManager).checkManager.disableCheck(HackType.BADPACKETS, true);
            }

            if (d < 17.0) {
               TPSManager.getPlugin(this.tpsManager).checkManager.disableCheck(HackType.FIGHTSPEED, true);
               TPSManager.getPlugin(this.tpsManager).checkManager.disableCheck(HackType.REACH, true);
               TPSManager.getPlugin(this.tpsManager).checkManager.disableCheck(HackType.ANGLE, true);
            } else if (d < 16.0) {
               TPSManager.getPlugin(this.tpsManager).checkManager.disableCheck(HackType.NORMALMOVEMENTS, true);
            } else if (d < 13.0) {
               TPSManager.getPlugin(this.tpsManager).verboseLogger.logViolation("(TPSManager) TPS is critical");
               TPSManager.getPlugin(this.tpsManager).checkManager.disableCheck(HackType.FLY, true);
               TPSManager.getPlugin(this.tpsManager).checkManager.disableCheck(HackType.GLIDE, true);
            } else if (d < 10.0) {
               TPSManager.getPlugin(this.tpsManager).checkManager.disableCheck(HackType.FORCEFIELD, true);
               TPSManager.getPlugin(this.tpsManager).checkManager.disableCheck(HackType.SPEED, true);
            }
         }
      }
   }
}
