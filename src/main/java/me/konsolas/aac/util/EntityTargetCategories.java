package me.konsolas.aac.util;

import org.bukkit.entity.EntityType;

public class EntityTargetCategories {
   public static boolean isReachTarget(EntityType entityType) {
      switch (EntityTypeSwitch.SWITCH_TABLE[entityType.ordinal()]) {
         case 1:
         case 2:
         case 3:
         case 4:
         case 5:
         case 6:
         case 7:
         case 8:
         case 9:
         case 10:
         case 11:
         case 12:
         case 13:
         case 14:
         case 15:
         case 16:
            return true;
         default:
            return false;
      }
   }

   public static boolean isKillauraTarget(EntityType entityType) {
      switch (EntityTypeSwitch.SWITCH_TABLE[entityType.ordinal()]) {
         case 1:
         case 2:
         case 3:
         case 4:
         case 5:
         case 6:
         case 7:
         case 8:
         case 9:
         case 10:
         case 11:
            return true;
         default:
            return false;
      }
   }

   public static boolean isPrimaryCombatTarget(EntityType entityType) {
      switch (EntityTypeSwitch.SWITCH_TABLE[entityType.ordinal()]) {
         case 1:
         case 2:
         case 3:
         case 4:
         case 5:
         case 6:
         case 7:
            return true;
         default:
            return false;
      }
   }

   public static boolean isKnockbackSource(EntityType entityType) {
      switch (EntityTypeSwitch.SWITCH_TABLE[entityType.ordinal()]) {
         case 17:
            return false;
         default:
            return true;
      }
   }

   class EntityTypeSwitch {
      static final int[] SWITCH_TABLE = new int[EntityType.values().length];

      static {
         try {
            SWITCH_TABLE[EntityType.PLAYER.ordinal()] = 1;
         } catch (NoSuchFieldError ex) {
         }

         try {
            SWITCH_TABLE[EntityType.VILLAGER.ordinal()] = 2;
         } catch (NoSuchFieldError ex2) {
         }

         try {
            SWITCH_TABLE[EntityType.ZOMBIE.ordinal()] = 3;
         } catch (NoSuchFieldError ex3) {
         }

         try {
            SWITCH_TABLE[EntityType.PIG_ZOMBIE.ordinal()] = 4;
         } catch (NoSuchFieldError ex4) {
         }

         try {
            SWITCH_TABLE[EntityType.SKELETON.ordinal()] = 5;
         } catch (NoSuchFieldError ex5) {
         }

         try {
            SWITCH_TABLE[EntityType.WITCH.ordinal()] = 6;
         } catch (NoSuchFieldError ex6) {
         }

         try {
            SWITCH_TABLE[EntityType.SNOWMAN.ordinal()] = 7;
         } catch (NoSuchFieldError ex7) {
         }

         try {
            SWITCH_TABLE[EntityType.BLAZE.ordinal()] = 8;
         } catch (NoSuchFieldError ex8) {
         }

         try {
            SWITCH_TABLE[EntityType.CREEPER.ordinal()] = 9;
         } catch (NoSuchFieldError ex9) {
         }

         try {
            SWITCH_TABLE[EntityType.CHICKEN.ordinal()] = 10;
         } catch (NoSuchFieldError ex10) {
         }

         try {
            SWITCH_TABLE[EntityType.ENDERMAN.ordinal()] = 11;
         } catch (NoSuchFieldError ex11) {
         }

         try {
            SWITCH_TABLE[EntityType.SILVERFISH.ordinal()] = 12;
         } catch (NoSuchFieldError ex12) {
         }

         try {
            SWITCH_TABLE[EntityType.ENDERMITE.ordinal()] = 13;
         } catch (NoSuchFieldError ex13) {
         }

         try {
            SWITCH_TABLE[EntityType.RABBIT.ordinal()] = 14;
         } catch (NoSuchFieldError ex14) {
         }

         try {
            SWITCH_TABLE[EntityType.BAT.ordinal()] = 15;
         } catch (NoSuchFieldError ex15) {
         }

         try {
            SWITCH_TABLE[EntityType.WOLF.ordinal()] = 16;
         } catch (NoSuchFieldError ex16) {
         }

         try {
            SWITCH_TABLE[EntityType.PRIMED_TNT.ordinal()] = 17;
         } catch (NoSuchFieldError ex17) {
         }
      }
   }
}
