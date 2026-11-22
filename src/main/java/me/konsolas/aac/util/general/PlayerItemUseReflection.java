package me.konsolas.aac.util.general;

import java.lang.reflect.Method;
import org.bukkit.entity.Player;

public class PlayerItemUseReflection {
   private static Method getHandleMethod;
   private static Method isUsingItemMethod;

   public static boolean isUsingItem(Player player) {
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
         if (isUsingItemMethod == null) {
            Class classVal2 = obj.getClass().getSuperclass();
            Class[] classValArray2 = new Class[0];
            isUsingItemMethod = classVal2.getDeclaredMethod("bS", classValArray2);
            boolean flag2 = true;
            Method method2 = isUsingItemMethod;
            method2.setAccessible(flag2);
         }

         return (Boolean)isUsingItemMethod.invoke(obj);
      } catch (Exception ex) {
         ex.printStackTrace();
         return false;
      }
   }
}
