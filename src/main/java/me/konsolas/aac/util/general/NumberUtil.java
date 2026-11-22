package me.konsolas.aac.util.general;

public class NumberUtil {
   public static boolean isWithinRange(double d, double d2, double d3) {
      return d + d3 > d2 && d - d3 < d2;
   }

   public static double pow(double d, double d2, int i) {
      for (int value = 0; value < i; value++) {
         d *= d2;
      }

      return d;
   }
}
