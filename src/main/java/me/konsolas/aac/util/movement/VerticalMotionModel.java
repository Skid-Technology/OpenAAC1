package me.konsolas.aac.util.movement;

import me.konsolas.aac.util.general.NumberUtil;

public class VerticalMotionModel {

   public static boolean isValidVerticalMotion(double d, boolean flag, boolean flag2, boolean flag3, int i, long ticks, double d2, double d3, int i2, double d4) {
      double d5 = predictNextDeltaY(d2);
      double d6 = i2 > 0 ? i2 / 10.0 + 0.42 : 0.42;
      if (!flag3) {
         if (flag) {
            return isWithinUpperBound(d, d6, d3);
         } else {
            return flag2 ? isWithinLowerBound(d, d5, d3) : isWithinTolerance(d, d5, d3);
         }
      } else if (flag) {
         return isWithinUpperBound(d, Math.max(d6, d4 + 0.05), d3);
      } else if (isWithinTolerance(d, d5, d3)) {
         return true;
      } else {
         return ticks < i * 50 ? isWithinUpperBound(d, d4 + 0.05, d3) : isWithinTolerance(d, d5, d3);
      }
   }

   public static boolean isWithinUpperBound(double d, double d2, double d3) {
      return d < d2 + d3;
   }

   public static boolean isWithinLowerBound(double d, double d2, double d3) {
      return d > d2 - d3;
   }

   public static boolean isWithinTolerance(double d, double d2, double d3) {
      return NumberUtil.isWithinRange(d, d2, d3);
   }

   public static double predictNextDeltaY(double d) {
      d -= 0.08;
      return d * 0.98;
   }

   public static double scaleVerticalEstimate(double d) {
      return d * 1.01;
   }
}
