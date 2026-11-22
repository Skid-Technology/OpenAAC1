package me.konsolas.aac.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.bukkit.entity.Player;

public class Ping {
   private static Method getHandleMethod;
   private static Field pingField;

   public static int getPing(Player player) {
      try {
         if (getHandleMethod == null) {
            Class classVal = player.getClass();
            Class[] classValArray = new Class[0];
            getHandleMethod = classVal.getDeclaredMethod("getHandle", classValArray);
            boolean flag = true;
            Method method = getHandleMethod;
            method.setAccessible(flag);
         }

         Object obj = getHandleMethod.invoke(player);
         if (pingField == null) {
            Class classVal2 = obj.getClass();
            String str = "ping";
            pingField = classVal2.getDeclaredField(str);
            boolean flag2 = true;
            Field field = pingField;
            field.setAccessible(flag2);
         }

         int value = pingField.getInt(obj);
         return value > 0 ? value : 0;
      } catch (Exception ex) {
         return 1;
      }
   }
}
