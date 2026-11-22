package me.konsolas.aac.util;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class TPS extends BukkitRunnable {
   private static TPS instance;
   int SAMPLE_COUNT = 4;
   private long lastTickTime = System.currentTimeMillis();
   private Deque<Long> tickDurations = new ArrayDeque<>(Collections.nCopies(this.SAMPLE_COUNT, 50L));

   private TPS(Plugin plugin) {
      this.runTaskTimer(plugin, 1L, 1L);
   }

   public static double getTps() {
      try {
         double d = 1000.0 / instance.getAverageTickMillis();
         return d > 20.0 ? 20.0 : d;
      } catch (Exception ex) {
         return 20.0;
      }
   }

   public static void startMonitoring(Plugin plugin) {
      instance = new TPS(plugin);
   }

   public void run() {
      long now = System.currentTimeMillis();
      long ticks = now - this.lastTickTime;
      this.lastTickTime = now;
      this.tickDurations.removeFirst();
      this.tickDurations.addLast(ticks);
   }

   public double getAverageTickMillis() {
      int value = 0;

      for (long ticks : this.tickDurations) {
         value = (int)(value + ticks);
      }

      return (double)value / this.SAMPLE_COUNT;
   }
}
