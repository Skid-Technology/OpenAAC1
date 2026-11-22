package me.konsolas.aac.util.general;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;

public class Reflection {
   private static String OBC_PREFIX = Bukkit.getServer().getClass().getPackage().getName();
   private static String NMS_PREFIX = OBC_PREFIX.replace("org.bukkit.craftbukkit", "net.minecraft.server");
   private static String NMS_VERSION = OBC_PREFIX.replace("org.bukkit.craftbukkit", "").replace(".", "");
   private static Pattern VARIABLE_PATTERN = Pattern.compile("\\{([^\\}]+)\\}");

   private Reflection() {
   }

   public static <T> FieldAccessor<T> getField(Class<?> classVal, String str, Class<T> classVal2) {
      return getFieldInternal(classVal, str, classVal2, 0);
   }

   public static <T> FieldAccessor<T> getField(String str, String str2, Class<T> classVal) {
      return getFieldInternal(getClass(str), str2, classVal, 0);
   }

   public static <T> FieldAccessor<T> getField(Class<?> classVal, Class<T> classVal2, int i) {
      return getFieldInternal(classVal, null, classVal2, i);
   }

   public static <T> FieldAccessor<T> getField(String str, Class<T> classVal, int i) {
      return getField(getClass(str), classVal, i);
   }

   private static <T> FieldAccessor<T> getFieldInternal(Class<?> classVal, String str, Class<T> classVal2, int i) {
      for (Field field : classVal.getDeclaredFields()) {
         if (str != null) {
            Field field2 = field;
            if (!field2.getName().equals(str)) {
               continue;
            }
         }

         Field field3 = field;
         if (classVal2.isAssignableFrom(field3.getType()) && i-- <= 0) {
            boolean flag = true;
            Field field4 = field;
            field4.setAccessible(flag);
            return new FieldAccessorImpl(field);
         }
      }

      if (classVal.getSuperclass() != null) {
         return getFieldInternal(classVal.getSuperclass(), str, classVal2, i);
      } else {
         throw new IllegalArgumentException("Cannot find field with type " + classVal2);
      }
   }

   public static MethodInvoker getMethod(String str, String str2, Class<?>... classValArray) {
      return getTypedMethod(getClass(str), str2, null, classValArray);
   }

   public static MethodInvoker getMethod(Class<?> classVal, String str, Class<?>... classValArray) {
      return getTypedMethod(classVal, str, null, classValArray);
   }

   public static MethodInvoker getTypedMethod(Class<?> classVal, String str, Class<?> classVal2, Class<?>... classValArray) {
      Method[] methodArray = classVal.getDeclaredMethods();
      int value2 = methodArray.length;
      int value3 = 0;

      Method method;
      while (true) {
         if (value3 >= value2) {
            if (classVal.getSuperclass() != null) {
               return getMethod(classVal.getSuperclass(), str, classValArray);
            }

            Object[] objArray = new Object[]{str, null};
            Class[] classValArray2 = classValArray;
            objArray[1] = Arrays.asList(classValArray2);
            throw new IllegalStateException(String.format("Unable to find method %s (%s).", objArray));
         }

         label27: {
            method = methodArray[value3];
            if (str != null) {
               Method method2 = method;
               if (!method2.getName().equals(str)) {
                  break label27;
               }
            }

            if (classVal2 == null) {
               break;
            }
         }

         Method method3 = method;
         if (method3.getReturnType().equals(classVal2)) {
            Method method4 = method;
            Class[] classValArray3 = method4.getParameterTypes();
            Class[] classValArray4 = classValArray;
            Class[] classValArray5 = classValArray3;
            if (Arrays.equals(classValArray5, classValArray4)) {
               break;
            }
         }

         value3++;
      }

      boolean flag = true;
      Method method5 = method;
      method5.setAccessible(flag);
      return new MethodInvokerImpl(method);
   }

   public static ConstructorInvoker getConstructor(String str, Class<?>... classValArray) {
      return getConstructor(getClass(str), classValArray);
   }

   public static ConstructorInvoker getConstructor(Class<?> classVal, Class<?>... classValArray) {
      for (Constructor constructor : classVal.getDeclaredConstructors()) {
         Constructor constructor2 = constructor;
         Class[] classValArray2 = constructor2.getParameterTypes();
         Class[] classValArray3 = classValArray;
         Class[] classValArray4 = classValArray2;
         if (Arrays.equals(classValArray4, classValArray3)) {
            boolean flag = true;
            Constructor constructor3 = constructor;
            constructor3.setAccessible(flag);
            return new ConstructorInvokerImpl(constructor);
         }
      }

      Object[] objArray = new Object[]{classVal, null};
      Class[] classValArray5 = classValArray;
      objArray[1] = Arrays.asList(classValArray5);
      throw new IllegalStateException(String.format("Unable to find constructor for %s (%s).", objArray));
   }

   public static Class<Object> getUntypedClass(String str) {
      return (Class<Object>)getClass(str);
   }

   public static Class<?> getClass(String str) {
      return getCanonicalClass(expandVariables(str));
   }

   public static Class<?> getMinecraftClass(String str) {
      return getCanonicalClass(NMS_PREFIX + "." + str);
   }

   public static Class<?> getCraftBukkitClass(String str) {
      return getCanonicalClass(OBC_PREFIX + "." + str);
   }

   private static Class<?> getCanonicalClass(String str) {
      try {
         return Class.forName(str);
      } catch (ClassNotFoundException ex) {
         throw new IllegalArgumentException("Cannot find " + str, ex);
      }
   }

   private static String expandVariables(String str) {
      StringBuffer stringBuffer = new StringBuffer();
      Matcher matcher = VARIABLE_PATTERN.matcher(str);

      while (matcher.find()) {
         String str2 = matcher.group(1);
         String str3 = "";
         if ("nms".equalsIgnoreCase(str2)) {
            str3 = NMS_PREFIX;
         } else if ("obc".equalsIgnoreCase(str2)) {
            str3 = OBC_PREFIX;
         } else {
            if (!"version".equalsIgnoreCase(str2)) {
               throw new IllegalArgumentException("Unknown variable: " + str2);
            }

            str3 = NMS_VERSION;
         }

         if (str3.length() > 0 && matcher.end() < str.length() && str.charAt(matcher.end()) != '.') {
            str3 = str3 + ".";
         }

         matcher.appendReplacement(stringBuffer, Matcher.quoteReplacement(str3));
      }

      matcher.appendTail(stringBuffer);
      return stringBuffer.toString();
   }

   private static final class FieldAccessorImpl<T> implements FieldAccessor<T> {
      final Field field;

      FieldAccessorImpl(Field field) {
         this.field = field;
      }

      @Override
      @SuppressWarnings("unchecked")
      public T get(Object obj) {
         try {
            return (T)this.field.get(obj);
         } catch (IllegalAccessException ex) {
            throw new RuntimeException("Cannot access reflection.", ex);
         }
      }

      @Override
      public void set(Object obj, Object obj2) {
         try {
            this.field.set(obj, obj2);
         } catch (IllegalAccessException ex) {
            throw new RuntimeException("Cannot access reflection.", ex);
         }
      }

      @Override
      public boolean hasField(Object obj) {
         return this.field.getDeclaringClass().isAssignableFrom(obj.getClass());
      }
   }

   private static final class MethodInvokerImpl implements MethodInvoker {
      final Method method;

      MethodInvokerImpl(Method method) {
         this.method = method;
      }

      @Override
      public Object invoke(Object obj, Object... objArray) {
         try {
            return this.method.invoke(obj, objArray);
         } catch (Exception ex) {
            throw new RuntimeException("Cannot invoke method " + this.method, ex);
         }
      }
   }

   private static final class ConstructorInvokerImpl implements ConstructorInvoker {
      final Constructor constructor;

      ConstructorInvokerImpl(Constructor constructor) {
         this.constructor = constructor;
      }

      @Override
      public Object invoke(Object... objArray) {
         try {
            return this.constructor.newInstance(objArray);
         } catch (Exception ex) {
            throw new RuntimeException("Cannot invoke constructor " + this.constructor, ex);
         }
      }
   }

   public interface ConstructorInvoker {
      Object invoke(Object... objArray);
   }

   public interface FieldAccessor<T> {
      T get(Object obj);

      void set(Object obj, Object obj2);

      boolean hasField(Object obj);
   }

   public interface MethodInvoker {
      Object invoke(Object obj, Object... objArray);
   }
}
