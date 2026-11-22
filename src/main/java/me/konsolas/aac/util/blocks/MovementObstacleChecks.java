package me.konsolas.aac.util.blocks;

import org.bukkit.Material;
import org.bukkit.block.Block;

public class MovementObstacleChecks {
   public static boolean isPassableBlock(Block block) {
      switch (MaterialSwitch.SWITCH_TABLE[block.getType().ordinal()]) {
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
         case 17:
         case 18:
         case 19:
         case 20:
         case 21:
         case 22:
         case 23:
         case 24:
         case 25:
         case 26:
         case 27:
         case 28:
         case 29:
         case 30:
         case 31:
         case 32:
         case 33:
         case 34:
         case 35:
         case 36:
         case 37:
         case 38:
         case 39:
         case 40:
         case 41:
         case 42:
         case 43:
         case 44:
         case 45:
         case 46:
         case 47:
         case 48:
         case 49:
         case 50:
         case 51:
         case 52:
         case 53:
         case 54:
         case 55:
         case 56:
         case 57:
         case 58:
         case 59:
         case 60:
         case 61:
         case 62:
         case 63:
         case 64:
         case 65:
         case 66:
         case 67:
         case 68:
         case 69:
         case 70:
         case 71:
         case 72:
         case 73:
         case 74:
         case 75:
         case 76:
         case 77:
         case 78:
         case 79:
         case 80:
         case 81:
         case 82:
         case 83:
         case 84:
         case 85:
         case 86:
         case 87:
         case 88:
         case 89:
         case 90:
         case 91:
         case 92:
         case 93:
         case 94:
         case 95:
         case 96:
         case 97:
         case 98:
            return true;
         default:
            return false;
      }
   }

   public static boolean isWaterloggedOrPartial(Block block) {
      switch (MaterialSwitch.SWITCH_TABLE[block.getType().ordinal()]) {
         case 99:
         case 100:
         case 101:
         case 102:
         case 103:
         case 104:
         case 105:
         case 106:
         case 107:
         case 108:
         case 109:
            return true;
         default:
            return false;
      }
   }

   public static boolean isFullCollisionBlock(Block block) {
      switch (MaterialSwitch.SWITCH_TABLE[block.getType().ordinal()]) {
         case 110:
         case 111:
         case 112:
         case 113:
         case 114:
         case 115:
            return true;
         default:
            return false;
      }
   }

   public static boolean isDoorOrTrapdoor(Block block) {
      switch (MaterialSwitch.SWITCH_TABLE[block.getType().ordinal()]) {
         case 116:
         case 117:
            return true;
         default:
            return false;
      }
   }

   public static boolean isCarpetOrPressurePlate(Block block) {
      switch (MaterialSwitch.SWITCH_TABLE[block.getType().ordinal()]) {
         case 118:
         case 119:
         case 120:
         case 121:
         case 122:
         case 123:
         case 124:
            return true;
         default:
            return false;
      }
   }

   public static boolean isPlayerUnobstructed(RelativeBlockCache relativeBlockCache) {
      return !BlockCollisionChecks.isSolidGroundBlock(relativeBlockCache.getRelativeBlock(0.0, 0.0, 0.3))
         && !BlockCollisionChecks.isSolidGroundBlock(relativeBlockCache.getRelativeBlock(0.3, 0.0, 0.0))
         && !BlockCollisionChecks.isSolidGroundBlock(relativeBlockCache.getRelativeBlock(0.0, 0.0, -0.3))
         && !BlockCollisionChecks.isSolidGroundBlock(relativeBlockCache.getRelativeBlock(-0.3, 0.0, 0.0))
         && !BlockCollisionChecks.isSolidGroundBlock(relativeBlockCache.getRelativeBlock(-0.3, 0.0, -0.3))
         && !BlockCollisionChecks.isSolidGroundBlock(relativeBlockCache.getRelativeBlock(-0.3, 0.0, 0.3))
         && !BlockCollisionChecks.isSolidGroundBlock(relativeBlockCache.getRelativeBlock(0.3, 0.0, -0.3))
         && !BlockCollisionChecks.isSolidGroundBlock(relativeBlockCache.getRelativeBlock(0.3, 0.0, 0.3))
         && !BlockCollisionChecks.isSolidGroundBlock(relativeBlockCache.getRelativeBlock(0.0, 1.68, 0.3))
         && !BlockCollisionChecks.isSolidGroundBlock(relativeBlockCache.getRelativeBlock(0.3, 1.68, 0.0))
         && !BlockCollisionChecks.isSolidGroundBlock(relativeBlockCache.getRelativeBlock(0.0, 1.68, -0.3))
         && !BlockCollisionChecks.isSolidGroundBlock(relativeBlockCache.getRelativeBlock(-0.3, 1.68, 0.0))
         && !BlockCollisionChecks.isSolidGroundBlock(relativeBlockCache.getRelativeBlock(-0.3, 1.68, -0.3))
         && !BlockCollisionChecks.isSolidGroundBlock(relativeBlockCache.getRelativeBlock(-0.3, 1.68, 0.3))
         && !BlockCollisionChecks.isSolidGroundBlock(relativeBlockCache.getRelativeBlock(0.3, 1.68, -0.3))
         && !BlockCollisionChecks.isSolidGroundBlock(relativeBlockCache.getRelativeBlock(0.3, 1.68, 0.3))
         && !BlockCollisionChecks.isSolidGroundBlock(relativeBlockCache.getRelativeBlock(0.0, 0.9, 0.3))
         && !BlockCollisionChecks.isSolidGroundBlock(relativeBlockCache.getRelativeBlock(0.3, 0.9, 0.0))
         && !BlockCollisionChecks.isSolidGroundBlock(relativeBlockCache.getRelativeBlock(0.0, 0.9, -0.3))
         && !BlockCollisionChecks.isSolidGroundBlock(relativeBlockCache.getRelativeBlock(-0.3, 0.9, 0.0))
         && !BlockCollisionChecks.isSolidGroundBlock(relativeBlockCache.getRelativeBlock(-0.3, 0.9, -0.3))
         && !BlockCollisionChecks.isSolidGroundBlock(relativeBlockCache.getRelativeBlock(-0.3, 0.9, 0.3))
         && !BlockCollisionChecks.isSolidGroundBlock(relativeBlockCache.getRelativeBlock(0.3, 0.9, -0.3))
         && !BlockCollisionChecks.isSolidGroundBlock(relativeBlockCache.getRelativeBlock(0.3, 0.9, 0.3));
   }

   public static boolean isFootPosClear(RelativeBlockCache relativeBlockCache) {
      return !BlockCollisionChecks.isStandableBlock(relativeBlockCache.getRelativeBlock(0.0, 0.0, 0.3))
         && !BlockCollisionChecks.isStandableBlock(relativeBlockCache.getRelativeBlock(0.3, 0.0, 0.0))
         && !BlockCollisionChecks.isStandableBlock(relativeBlockCache.getRelativeBlock(0.0, 0.0, -0.3))
         && !BlockCollisionChecks.isStandableBlock(relativeBlockCache.getRelativeBlock(-0.3, 0.0, 0.0))
         && !BlockCollisionChecks.isStandableBlock(relativeBlockCache.getRelativeBlock(-0.3, 0.0, -0.3))
         && !BlockCollisionChecks.isStandableBlock(relativeBlockCache.getRelativeBlock(-0.3, 0.0, 0.3))
         && !BlockCollisionChecks.isStandableBlock(relativeBlockCache.getRelativeBlock(0.3, 0.0, -0.3))
         && !BlockCollisionChecks.isStandableBlock(relativeBlockCache.getRelativeBlock(0.3, 0.0, 0.3))
         && !BlockCollisionChecks.isStandableBlock(relativeBlockCache.getRelativeBlock(0.0, 1.68, 0.3))
         && !BlockCollisionChecks.isStandableBlock(relativeBlockCache.getRelativeBlock(0.3, 1.68, 0.0))
         && !BlockCollisionChecks.isStandableBlock(relativeBlockCache.getRelativeBlock(0.0, 1.68, -0.3))
         && !BlockCollisionChecks.isStandableBlock(relativeBlockCache.getRelativeBlock(-0.3, 1.68, 0.0))
         && !BlockCollisionChecks.isStandableBlock(relativeBlockCache.getRelativeBlock(-0.3, 1.68, -0.3))
         && !BlockCollisionChecks.isStandableBlock(relativeBlockCache.getRelativeBlock(-0.3, 1.68, 0.3))
         && !BlockCollisionChecks.isStandableBlock(relativeBlockCache.getRelativeBlock(0.3, 1.68, -0.3))
         && !BlockCollisionChecks.isStandableBlock(relativeBlockCache.getRelativeBlock(0.3, 1.68, 0.3))
         && !BlockCollisionChecks.isStandableBlock(relativeBlockCache.getRelativeBlock(0.0, 0.9, 0.3))
         && !BlockCollisionChecks.isStandableBlock(relativeBlockCache.getRelativeBlock(0.3, 0.9, 0.0))
         && !BlockCollisionChecks.isStandableBlock(relativeBlockCache.getRelativeBlock(0.0, 0.9, -0.3))
         && !BlockCollisionChecks.isStandableBlock(relativeBlockCache.getRelativeBlock(-0.3, 0.9, 0.0))
         && !BlockCollisionChecks.isStandableBlock(relativeBlockCache.getRelativeBlock(-0.3, 0.9, -0.3))
         && !BlockCollisionChecks.isStandableBlock(relativeBlockCache.getRelativeBlock(-0.3, 0.9, 0.3))
         && !BlockCollisionChecks.isStandableBlock(relativeBlockCache.getRelativeBlock(0.3, 0.9, -0.3))
         && !BlockCollisionChecks.isStandableBlock(relativeBlockCache.getRelativeBlock(0.3, 0.9, 0.3));
   }

   public static boolean hasSolidGroundAround(RelativeBlockCache relativeBlockCache) {
      return BlockCollisionChecks.isSolidGroundBlock(relativeBlockCache.getRelativeBlock(0.0, 0.0, 0.4))
         && BlockCollisionChecks.isSolidGroundBlock(relativeBlockCache.getRelativeBlock(0.4, 0.0, 0.0))
         && BlockCollisionChecks.isSolidGroundBlock(relativeBlockCache.getRelativeBlock(0.0, 0.0, -0.4))
         && BlockCollisionChecks.isSolidGroundBlock(relativeBlockCache.getRelativeBlock(-0.4, 0.0, 0.0))
         && BlockCollisionChecks.isSolidGroundBlock(relativeBlockCache.getRelativeBlock(-0.4, 0.0, -0.4))
         && BlockCollisionChecks.isSolidGroundBlock(relativeBlockCache.getRelativeBlock(-0.4, 0.0, 0.4))
         && BlockCollisionChecks.isSolidGroundBlock(relativeBlockCache.getRelativeBlock(0.4, 0.0, -0.4))
         && BlockCollisionChecks.isSolidGroundBlock(relativeBlockCache.getRelativeBlock(0.4, 0.0, 0.4))
         && BlockCollisionChecks.isSolidGroundBlock(relativeBlockCache.getRelativeBlock(0.0, 2.01, 0.4))
         && BlockCollisionChecks.isSolidGroundBlock(relativeBlockCache.getRelativeBlock(0.4, 2.01, 0.0))
         && BlockCollisionChecks.isSolidGroundBlock(relativeBlockCache.getRelativeBlock(0.0, 2.01, -0.4))
         && BlockCollisionChecks.isSolidGroundBlock(relativeBlockCache.getRelativeBlock(-0.4, 2.01, 0.0))
         && BlockCollisionChecks.isSolidGroundBlock(relativeBlockCache.getRelativeBlock(-0.4, 2.01, -0.4))
         && BlockCollisionChecks.isSolidGroundBlock(relativeBlockCache.getRelativeBlock(-0.4, 2.01, 0.4))
         && BlockCollisionChecks.isSolidGroundBlock(relativeBlockCache.getRelativeBlock(0.4, 2.01, -0.4))
         && BlockCollisionChecks.isSolidGroundBlock(relativeBlockCache.getRelativeBlock(0.4, 2.01, 0.4));
   }

   class MaterialSwitch {
      static final int[] SWITCH_TABLE = new int[Material.values().length];

      static {
         try {
            SWITCH_TABLE[Material.ACACIA_STAIRS.ordinal()] = 1;
         } catch (NoSuchFieldError ex) {
         }

         try {
            SWITCH_TABLE[Material.ACTIVATOR_RAIL.ordinal()] = 2;
         } catch (NoSuchFieldError ex2) {
         }

         try {
            SWITCH_TABLE[Material.AIR.ordinal()] = 3;
         } catch (NoSuchFieldError ex3) {
         }

         try {
            SWITCH_TABLE[Material.ANVIL.ordinal()] = 4;
         } catch (NoSuchFieldError ex4) {
         }

         try {
            SWITCH_TABLE[Material.ARMOR_STAND.ordinal()] = 5;
         } catch (NoSuchFieldError ex5) {
         }

         try {
            SWITCH_TABLE[Material.BANNER.ordinal()] = 6;
         } catch (NoSuchFieldError ex6) {
         }

         try {
            SWITCH_TABLE[Material.BEACON.ordinal()] = 7;
         } catch (NoSuchFieldError ex7) {
         }

         try {
            SWITCH_TABLE[Material.BED_BLOCK.ordinal()] = 8;
         } catch (NoSuchFieldError ex8) {
         }

         try {
            SWITCH_TABLE[Material.BIRCH_WOOD_STAIRS.ordinal()] = 9;
         } catch (NoSuchFieldError ex9) {
         }

         try {
            SWITCH_TABLE[Material.BREWING_STAND.ordinal()] = 10;
         } catch (NoSuchFieldError ex10) {
         }

         try {
            SWITCH_TABLE[Material.BRICK_STAIRS.ordinal()] = 11;
         } catch (NoSuchFieldError ex11) {
         }

         try {
            SWITCH_TABLE[Material.BROWN_MUSHROOM.ordinal()] = 12;
         } catch (NoSuchFieldError ex12) {
         }

         try {
            SWITCH_TABLE[Material.CACTUS.ordinal()] = 13;
         } catch (NoSuchFieldError ex13) {
         }

         try {
            SWITCH_TABLE[Material.CAKE_BLOCK.ordinal()] = 14;
         } catch (NoSuchFieldError ex14) {
         }

         try {
            SWITCH_TABLE[Material.CARPET.ordinal()] = 15;
         } catch (NoSuchFieldError ex15) {
         }

         try {
            SWITCH_TABLE[Material.CARROT.ordinal()] = 16;
         } catch (NoSuchFieldError ex16) {
         }

         try {
            SWITCH_TABLE[Material.CAULDRON.ordinal()] = 17;
         } catch (NoSuchFieldError ex17) {
         }

         try {
            SWITCH_TABLE[Material.CHEST.ordinal()] = 18;
         } catch (NoSuchFieldError ex18) {
         }

         try {
            SWITCH_TABLE[Material.COBBLESTONE_STAIRS.ordinal()] = 19;
         } catch (NoSuchFieldError ex19) {
         }

         try {
            SWITCH_TABLE[Material.COCOA.ordinal()] = 20;
         } catch (NoSuchFieldError ex20) {
         }

         try {
            SWITCH_TABLE[Material.CROPS.ordinal()] = 21;
         } catch (NoSuchFieldError ex21) {
         }

         try {
            SWITCH_TABLE[Material.DARK_OAK_STAIRS.ordinal()] = 22;
         } catch (NoSuchFieldError ex22) {
         }

         try {
            SWITCH_TABLE[Material.DAYLIGHT_DETECTOR.ordinal()] = 23;
         } catch (NoSuchFieldError ex23) {
         }

         try {
            SWITCH_TABLE[Material.DAYLIGHT_DETECTOR_INVERTED.ordinal()] = 24;
         } catch (NoSuchFieldError ex24) {
         }

         try {
            SWITCH_TABLE[Material.DEAD_BUSH.ordinal()] = 25;
         } catch (NoSuchFieldError ex25) {
         }

         try {
            SWITCH_TABLE[Material.DETECTOR_RAIL.ordinal()] = 26;
         } catch (NoSuchFieldError ex26) {
         }

         try {
            SWITCH_TABLE[Material.DIODE_BLOCK_OFF.ordinal()] = 27;
         } catch (NoSuchFieldError ex27) {
         }

         try {
            SWITCH_TABLE[Material.DIODE_BLOCK_ON.ordinal()] = 28;
         } catch (NoSuchFieldError ex28) {
         }

         try {
            SWITCH_TABLE[Material.DOUBLE_PLANT.ordinal()] = 29;
         } catch (NoSuchFieldError ex29) {
         }

         try {
            SWITCH_TABLE[Material.DRAGON_EGG.ordinal()] = 30;
         } catch (NoSuchFieldError ex30) {
         }

         try {
            SWITCH_TABLE[Material.ENCHANTMENT_TABLE.ordinal()] = 31;
         } catch (NoSuchFieldError ex31) {
         }

         try {
            SWITCH_TABLE[Material.ENDER_CHEST.ordinal()] = 32;
         } catch (NoSuchFieldError ex32) {
         }

         try {
            SWITCH_TABLE[Material.ENDER_PORTAL.ordinal()] = 33;
         } catch (NoSuchFieldError ex33) {
         }

         try {
            SWITCH_TABLE[Material.ENDER_PORTAL_FRAME.ordinal()] = 34;
         } catch (NoSuchFieldError ex34) {
         }

         try {
            SWITCH_TABLE[Material.FIRE.ordinal()] = 35;
         } catch (NoSuchFieldError ex35) {
         }

         try {
            SWITCH_TABLE[Material.FLOWER_POT.ordinal()] = 36;
         } catch (NoSuchFieldError ex36) {
         }

         try {
            SWITCH_TABLE[Material.GOLD_PLATE.ordinal()] = 37;
         } catch (NoSuchFieldError ex37) {
         }

         try {
            SWITCH_TABLE[Material.HOPPER.ordinal()] = 38;
         } catch (NoSuchFieldError ex38) {
         }

         try {
            SWITCH_TABLE[Material.IRON_PLATE.ordinal()] = 39;
         } catch (NoSuchFieldError ex39) {
         }

         try {
            SWITCH_TABLE[Material.JUNGLE_WOOD_STAIRS.ordinal()] = 40;
         } catch (NoSuchFieldError ex40) {
         }

         try {
            SWITCH_TABLE[Material.LADDER.ordinal()] = 41;
         } catch (NoSuchFieldError ex41) {
         }

         try {
            SWITCH_TABLE[Material.LAVA.ordinal()] = 42;
         } catch (NoSuchFieldError ex42) {
         }

         try {
            SWITCH_TABLE[Material.LEVER.ordinal()] = 43;
         } catch (NoSuchFieldError ex43) {
         }

         try {
            SWITCH_TABLE[Material.LONG_GRASS.ordinal()] = 44;
         } catch (NoSuchFieldError ex44) {
         }

         try {
            SWITCH_TABLE[Material.MELON_STEM.ordinal()] = 45;
         } catch (NoSuchFieldError ex45) {
         }

         try {
            SWITCH_TABLE[Material.NETHER_BRICK_STAIRS.ordinal()] = 46;
         } catch (NoSuchFieldError ex46) {
         }

         try {
            SWITCH_TABLE[Material.NETHER_WARTS.ordinal()] = 47;
         } catch (NoSuchFieldError ex47) {
         }

         try {
            SWITCH_TABLE[Material.PISTON_BASE.ordinal()] = 48;
         } catch (NoSuchFieldError ex48) {
         }

         try {
            SWITCH_TABLE[Material.PISTON_EXTENSION.ordinal()] = 49;
         } catch (NoSuchFieldError ex49) {
         }

         try {
            SWITCH_TABLE[Material.PISTON_MOVING_PIECE.ordinal()] = 50;
         } catch (NoSuchFieldError ex50) {
         }

         try {
            SWITCH_TABLE[Material.PISTON_STICKY_BASE.ordinal()] = 51;
         } catch (NoSuchFieldError ex51) {
         }

         try {
            SWITCH_TABLE[Material.PORTAL.ordinal()] = 52;
         } catch (NoSuchFieldError ex52) {
         }

         try {
            SWITCH_TABLE[Material.POTATO.ordinal()] = 53;
         } catch (NoSuchFieldError ex53) {
         }

         try {
            SWITCH_TABLE[Material.POWERED_RAIL.ordinal()] = 54;
         } catch (NoSuchFieldError ex54) {
         }

         try {
            SWITCH_TABLE[Material.PUMPKIN_STEM.ordinal()] = 55;
         } catch (NoSuchFieldError ex55) {
         }

         try {
            SWITCH_TABLE[Material.QUARTZ_STAIRS.ordinal()] = 56;
         } catch (NoSuchFieldError ex56) {
         }

         try {
            SWITCH_TABLE[Material.RAILS.ordinal()] = 57;
         } catch (NoSuchFieldError ex57) {
         }

         try {
            SWITCH_TABLE[Material.RED_MUSHROOM.ordinal()] = 58;
         } catch (NoSuchFieldError ex58) {
         }

         try {
            SWITCH_TABLE[Material.RED_ROSE.ordinal()] = 59;
         } catch (NoSuchFieldError ex59) {
         }

         try {
            SWITCH_TABLE[Material.RED_SANDSTONE_STAIRS.ordinal()] = 60;
         } catch (NoSuchFieldError ex60) {
         }

         try {
            SWITCH_TABLE[Material.REDSTONE_COMPARATOR_OFF.ordinal()] = 61;
         } catch (NoSuchFieldError ex61) {
         }

         try {
            SWITCH_TABLE[Material.REDSTONE_COMPARATOR_ON.ordinal()] = 62;
         } catch (NoSuchFieldError ex62) {
         }

         try {
            SWITCH_TABLE[Material.REDSTONE_TORCH_OFF.ordinal()] = 63;
         } catch (NoSuchFieldError ex63) {
         }

         try {
            SWITCH_TABLE[Material.REDSTONE_TORCH_ON.ordinal()] = 64;
         } catch (NoSuchFieldError ex64) {
         }

         try {
            SWITCH_TABLE[Material.REDSTONE_WIRE.ordinal()] = 65;
         } catch (NoSuchFieldError ex65) {
         }

         try {
            SWITCH_TABLE[Material.SANDSTONE_STAIRS.ordinal()] = 66;
         } catch (NoSuchFieldError ex66) {
         }

         try {
            SWITCH_TABLE[Material.SAPLING.ordinal()] = 67;
         } catch (NoSuchFieldError ex67) {
         }

         try {
            SWITCH_TABLE[Material.SEEDS.ordinal()] = 68;
         } catch (NoSuchFieldError ex68) {
         }

         try {
            SWITCH_TABLE[Material.SIGN.ordinal()] = 69;
         } catch (NoSuchFieldError ex69) {
         }

         try {
            SWITCH_TABLE[Material.SIGN_POST.ordinal()] = 70;
         } catch (NoSuchFieldError ex70) {
         }

         try {
            SWITCH_TABLE[Material.SKULL.ordinal()] = 71;
         } catch (NoSuchFieldError ex71) {
         }

         try {
            SWITCH_TABLE[Material.SMOOTH_STAIRS.ordinal()] = 72;
         } catch (NoSuchFieldError ex72) {
         }

         try {
            SWITCH_TABLE[Material.SNOW.ordinal()] = 73;
         } catch (NoSuchFieldError ex73) {
         }

         try {
            SWITCH_TABLE[Material.SOUL_SAND.ordinal()] = 74;
         } catch (NoSuchFieldError ex74) {
         }

         try {
            SWITCH_TABLE[Material.SPRUCE_WOOD_STAIRS.ordinal()] = 75;
         } catch (NoSuchFieldError ex75) {
         }

         try {
            SWITCH_TABLE[Material.STANDING_BANNER.ordinal()] = 76;
         } catch (NoSuchFieldError ex76) {
         }

         try {
            SWITCH_TABLE[Material.STATIONARY_LAVA.ordinal()] = 77;
         } catch (NoSuchFieldError ex77) {
         }

         try {
            SWITCH_TABLE[Material.STATIONARY_WATER.ordinal()] = 78;
         } catch (NoSuchFieldError ex78) {
         }

         try {
            SWITCH_TABLE[Material.STEP.ordinal()] = 79;
         } catch (NoSuchFieldError ex79) {
         }

         try {
            SWITCH_TABLE[Material.STONE_BUTTON.ordinal()] = 80;
         } catch (NoSuchFieldError ex80) {
         }

         try {
            SWITCH_TABLE[Material.STONE_PLATE.ordinal()] = 81;
         } catch (NoSuchFieldError ex81) {
         }

         try {
            SWITCH_TABLE[Material.STONE_SLAB2.ordinal()] = 82;
         } catch (NoSuchFieldError ex82) {
         }

         try {
            SWITCH_TABLE[Material.SUGAR_CANE_BLOCK.ordinal()] = 83;
         } catch (NoSuchFieldError ex83) {
         }

         try {
            SWITCH_TABLE[Material.TORCH.ordinal()] = 84;
         } catch (NoSuchFieldError ex84) {
         }

         try {
            SWITCH_TABLE[Material.TRAPPED_CHEST.ordinal()] = 85;
         } catch (NoSuchFieldError ex85) {
         }

         try {
            SWITCH_TABLE[Material.TRIPWIRE.ordinal()] = 86;
         } catch (NoSuchFieldError ex86) {
         }

         try {
            SWITCH_TABLE[Material.TRIPWIRE_HOOK.ordinal()] = 87;
         } catch (NoSuchFieldError ex87) {
         }

         try {
            SWITCH_TABLE[Material.VINE.ordinal()] = 88;
         } catch (NoSuchFieldError ex88) {
         }

         try {
            SWITCH_TABLE[Material.WALL_BANNER.ordinal()] = 89;
         } catch (NoSuchFieldError ex89) {
         }

         try {
            SWITCH_TABLE[Material.WALL_SIGN.ordinal()] = 90;
         } catch (NoSuchFieldError ex90) {
         }

         try {
            SWITCH_TABLE[Material.WATER.ordinal()] = 91;
         } catch (NoSuchFieldError ex91) {
         }

         try {
            SWITCH_TABLE[Material.WATER_LILY.ordinal()] = 92;
         } catch (NoSuchFieldError ex92) {
         }

         try {
            SWITCH_TABLE[Material.WEB.ordinal()] = 93;
         } catch (NoSuchFieldError ex93) {
         }

         try {
            SWITCH_TABLE[Material.WOOD_BUTTON.ordinal()] = 94;
         } catch (NoSuchFieldError ex94) {
         }

         try {
            SWITCH_TABLE[Material.WOOD_PLATE.ordinal()] = 95;
         } catch (NoSuchFieldError ex95) {
         }

         try {
            SWITCH_TABLE[Material.WOOD_STAIRS.ordinal()] = 96;
         } catch (NoSuchFieldError ex96) {
         }

         try {
            SWITCH_TABLE[Material.WOOD_STEP.ordinal()] = 97;
         } catch (NoSuchFieldError ex97) {
         }

         try {
            SWITCH_TABLE[Material.YELLOW_FLOWER.ordinal()] = 98;
         } catch (NoSuchFieldError ex98) {
         }

         try {
            SWITCH_TABLE[Material.ACACIA_FENCE.ordinal()] = 99;
         } catch (NoSuchFieldError ex99) {
         }

         try {
            SWITCH_TABLE[Material.BIRCH_FENCE.ordinal()] = 100;
         } catch (NoSuchFieldError ex100) {
         }

         try {
            SWITCH_TABLE[Material.COBBLE_WALL.ordinal()] = 101;
         } catch (NoSuchFieldError ex101) {
         }

         try {
            SWITCH_TABLE[Material.DARK_OAK_FENCE.ordinal()] = 102;
         } catch (NoSuchFieldError ex102) {
         }

         try {
            SWITCH_TABLE[Material.FENCE.ordinal()] = 103;
         } catch (NoSuchFieldError ex103) {
         }

         try {
            SWITCH_TABLE[Material.IRON_FENCE.ordinal()] = 104;
         } catch (NoSuchFieldError ex104) {
         }

         try {
            SWITCH_TABLE[Material.JUNGLE_FENCE.ordinal()] = 105;
         } catch (NoSuchFieldError ex105) {
         }

         try {
            SWITCH_TABLE[Material.NETHER_FENCE.ordinal()] = 106;
         } catch (NoSuchFieldError ex106) {
         }

         try {
            SWITCH_TABLE[Material.SPRUCE_FENCE.ordinal()] = 107;
         } catch (NoSuchFieldError ex107) {
         }

         try {
            SWITCH_TABLE[Material.STAINED_GLASS_PANE.ordinal()] = 108;
         } catch (NoSuchFieldError ex108) {
         }

         try {
            SWITCH_TABLE[Material.THIN_GLASS.ordinal()] = 109;
         } catch (NoSuchFieldError ex109) {
         }

         try {
            SWITCH_TABLE[Material.ACACIA_FENCE_GATE.ordinal()] = 110;
         } catch (NoSuchFieldError ex110) {
         }

         try {
            SWITCH_TABLE[Material.BIRCH_FENCE_GATE.ordinal()] = 111;
         } catch (NoSuchFieldError ex111) {
         }

         try {
            SWITCH_TABLE[Material.DARK_OAK_FENCE_GATE.ordinal()] = 112;
         } catch (NoSuchFieldError ex112) {
         }

         try {
            SWITCH_TABLE[Material.FENCE_GATE.ordinal()] = 113;
         } catch (NoSuchFieldError ex113) {
         }

         try {
            SWITCH_TABLE[Material.JUNGLE_FENCE_GATE.ordinal()] = 114;
         } catch (NoSuchFieldError ex114) {
         }

         try {
            SWITCH_TABLE[Material.SPRUCE_FENCE_GATE.ordinal()] = 115;
         } catch (NoSuchFieldError ex115) {
         }

         try {
            SWITCH_TABLE[Material.TRAP_DOOR.ordinal()] = 116;
         } catch (NoSuchFieldError ex116) {
         }

         try {
            SWITCH_TABLE[Material.IRON_TRAPDOOR.ordinal()] = 117;
         } catch (NoSuchFieldError ex117) {
         }

         try {
            SWITCH_TABLE[Material.ACACIA_DOOR.ordinal()] = 118;
         } catch (NoSuchFieldError ex118) {
         }

         try {
            SWITCH_TABLE[Material.BIRCH_DOOR.ordinal()] = 119;
         } catch (NoSuchFieldError ex119) {
         }

         try {
            SWITCH_TABLE[Material.DARK_OAK_DOOR.ordinal()] = 120;
         } catch (NoSuchFieldError ex120) {
         }

         try {
            SWITCH_TABLE[Material.IRON_DOOR_BLOCK.ordinal()] = 121;
         } catch (NoSuchFieldError ex121) {
         }

         try {
            SWITCH_TABLE[Material.JUNGLE_DOOR.ordinal()] = 122;
         } catch (NoSuchFieldError ex122) {
         }

         try {
            SWITCH_TABLE[Material.SPRUCE_DOOR.ordinal()] = 123;
         } catch (NoSuchFieldError ex123) {
         }

         try {
            SWITCH_TABLE[Material.WOODEN_DOOR.ordinal()] = 124;
         } catch (NoSuchFieldError ex124) {
         }
      }
   }
}
