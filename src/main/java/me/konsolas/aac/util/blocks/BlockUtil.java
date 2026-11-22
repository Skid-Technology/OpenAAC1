package me.konsolas.aac.util.blocks;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class BlockUtil {
   public static long getBreakTicks(Material material, Player player) {
      int value = 0;
      int value2 = 0;

      for (PotionEffect potionEffect : player.getActivePotionEffects()) {
         if (potionEffect.getType().equals(PotionEffectType.SLOW_DIGGING)) {
            value2 = potionEffect.getAmplifier() + 1;
         } else if (potionEffect.getType().equals(PotionEffectType.FAST_DIGGING)) {
            value = potionEffect.getAmplifier() + 1;
         }
      }

      try {
         return computeBreakTicks(material, new MiningTool(player.getItemInHand()), new LegacyBlockIdentifier(value, value2), false);
      } catch (NullPointerException ex) {
         return 0L;
      }
   }

   public static int getHasteLevel(Player player) {
      for (PotionEffect potionEffect : player.getActivePotionEffects()) {
         if (potionEffect.getType().equals(PotionEffectType.FAST_DIGGING)) {
            return potionEffect.getAmplifier() + 1;
         }
      }

      return 0;
   }

   public static long computeBreakTicks(Material material, MiningTool miningTool, LegacyBlockIdentifier legacyBlockIdentifier, boolean flag) {
      if (BlockData.getHardness(material) == Double.POSITIVE_INFINITY) {
         return 2147483647L;
      }

      double d = BlockData.getHardness(material) * (canHarvestEfficiently(material, miningTool) ? 30 : 100);
      double d2 = 1.0;
      if (isEffectiveTool(material, miningTool)) {
         switch (ToolLevelMaterialSwitch.SWITCH_TABLE[miningTool.getLevel().ordinal()]) {
            case 1:
               d2 = 2.0;
               break;
            case 2:
               d2 = 12.0;
               break;
            case 3:
               d2 = 4.0;
               break;
            case 4:
               d2 = 6.0;
               break;
            case 5:
               d2 = 8.0;
               break;
            case 6:
               d2 = 1.0;
         }

         if (miningTool.getCategory() == MiningToolCategory.SHEARS) {
            d2 = 15.0;
         }

         if (miningTool.getEfficiencyLevel() != 0) {
            d2 += Math.pow(miningTool.getEfficiencyLevel(), 2.0) + 1.0;
         }

         if (flag) {
            d2 /= 5.0;
         }
      } else {
         d2 = 1.0;
      }

      if (legacyBlockIdentifier.getHasteAmplifier() > 0) {
         d2 += d2 * (legacyBlockIdentifier.getHasteAmplifier() * 0.2);
      }

      if (legacyBlockIdentifier.getFatigueAmplifier() > 0) {
         switch (legacyBlockIdentifier.getFatigueAmplifier()) {
            case 1:
               d2 *= 0.3;
               break;
            case 2:
               d2 *= 0.09;
               break;
            case 3:
               d2 *= 0.0027;
               break;
            default:
               d2 *= 8.1E-4;
         }
      }

      double d3 = d / d2;
      return (int)d3;
   }

   public static boolean canHarvestEfficiently(Material material, MiningTool miningTool) {
      return BlockData.getToolCategory(material) == miningTool.getCategory() && ToolMiningLevel.isRightLevel(BlockData.getMinimumLevel(material), miningTool.getLevel())
         ? true
         : BlockData.getMinimumLevel(material) == ToolMiningLevel.NA || BlockData.isFullBlock(material);
   }

   public static boolean isEffectiveTool(Material material, MiningTool miningTool) {
      return canHarvestEfficiently(material, miningTool);
   }

   public static boolean isTransparent(Material material) {
      if (!material.isBlock()) {
         throw new IllegalArgumentException("Tried to check transparency of non-block");
      }

      if (!material.toString().contains("DOOR") && !material.toString().contains("FENCE") && !material.toString().contains("STAIRS")) {
         switch (ToolLevelMaterialSwitch.MATERIAL_SWITCH_TABLE[material.ordinal()]) {
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
               return true;
            default:
               return false;
         }
      } else {
         return true;
      }
   }

   class ToolLevelMaterialSwitch {
      static final int[] SWITCH_TABLE;
      static final int[] MATERIAL_SWITCH_TABLE = new int[Material.values().length];

      static {
         try {
            MATERIAL_SWITCH_TABLE[Material.AIR.ordinal()] = 1;
         } catch (NoSuchFieldError ex) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.SAPLING.ordinal()] = 2;
         } catch (NoSuchFieldError ex2) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.POWERED_RAIL.ordinal()] = 3;
         } catch (NoSuchFieldError ex3) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.DETECTOR_RAIL.ordinal()] = 4;
         } catch (NoSuchFieldError ex4) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.LONG_GRASS.ordinal()] = 5;
         } catch (NoSuchFieldError ex5) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.DEAD_BUSH.ordinal()] = 6;
         } catch (NoSuchFieldError ex6) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.YELLOW_FLOWER.ordinal()] = 7;
         } catch (NoSuchFieldError ex7) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.RED_ROSE.ordinal()] = 8;
         } catch (NoSuchFieldError ex8) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.BROWN_MUSHROOM.ordinal()] = 9;
         } catch (NoSuchFieldError ex9) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.RED_MUSHROOM.ordinal()] = 10;
         } catch (NoSuchFieldError ex10) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.TORCH.ordinal()] = 11;
         } catch (NoSuchFieldError ex11) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.FIRE.ordinal()] = 12;
         } catch (NoSuchFieldError ex12) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.REDSTONE_WIRE.ordinal()] = 13;
         } catch (NoSuchFieldError ex13) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.CROPS.ordinal()] = 14;
         } catch (NoSuchFieldError ex14) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.LADDER.ordinal()] = 15;
         } catch (NoSuchFieldError ex15) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.RAILS.ordinal()] = 16;
         } catch (NoSuchFieldError ex16) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.LEVER.ordinal()] = 17;
         } catch (NoSuchFieldError ex17) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.REDSTONE_TORCH_OFF.ordinal()] = 18;
         } catch (NoSuchFieldError ex18) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.REDSTONE_TORCH_ON.ordinal()] = 19;
         } catch (NoSuchFieldError ex19) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.STONE_BUTTON.ordinal()] = 20;
         } catch (NoSuchFieldError ex20) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.SNOW.ordinal()] = 21;
         } catch (NoSuchFieldError ex21) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.SUGAR_CANE_BLOCK.ordinal()] = 22;
         } catch (NoSuchFieldError ex22) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.PORTAL.ordinal()] = 23;
         } catch (NoSuchFieldError ex23) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.DIODE_BLOCK_OFF.ordinal()] = 24;
         } catch (NoSuchFieldError ex24) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.DIODE_BLOCK_ON.ordinal()] = 25;
         } catch (NoSuchFieldError ex25) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.PUMPKIN_STEM.ordinal()] = 26;
         } catch (NoSuchFieldError ex26) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.MELON_STEM.ordinal()] = 27;
         } catch (NoSuchFieldError ex27) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.VINE.ordinal()] = 28;
         } catch (NoSuchFieldError ex28) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.WATER_LILY.ordinal()] = 29;
         } catch (NoSuchFieldError ex29) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.NETHER_WARTS.ordinal()] = 30;
         } catch (NoSuchFieldError ex30) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.ENDER_PORTAL.ordinal()] = 31;
         } catch (NoSuchFieldError ex31) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.COCOA.ordinal()] = 32;
         } catch (NoSuchFieldError ex32) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.TRIPWIRE_HOOK.ordinal()] = 33;
         } catch (NoSuchFieldError ex33) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.TRIPWIRE.ordinal()] = 34;
         } catch (NoSuchFieldError ex34) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.FLOWER_POT.ordinal()] = 35;
         } catch (NoSuchFieldError ex35) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.CARROT.ordinal()] = 36;
         } catch (NoSuchFieldError ex36) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.POTATO.ordinal()] = 37;
         } catch (NoSuchFieldError ex37) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.WOOD_BUTTON.ordinal()] = 38;
         } catch (NoSuchFieldError ex38) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.SKULL.ordinal()] = 39;
         } catch (NoSuchFieldError ex39) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.REDSTONE_COMPARATOR_OFF.ordinal()] = 40;
         } catch (NoSuchFieldError ex40) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.REDSTONE_COMPARATOR_ON.ordinal()] = 41;
         } catch (NoSuchFieldError ex41) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.ACTIVATOR_RAIL.ordinal()] = 42;
         } catch (NoSuchFieldError ex42) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.CARPET.ordinal()] = 43;
         } catch (NoSuchFieldError ex43) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.DOUBLE_PLANT.ordinal()] = 44;
         } catch (NoSuchFieldError ex44) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.LEAVES.ordinal()] = 45;
         } catch (NoSuchFieldError ex45) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.LEAVES_2.ordinal()] = 46;
         } catch (NoSuchFieldError ex46) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.WATER.ordinal()] = 47;
         } catch (NoSuchFieldError ex47) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.STATIONARY_WATER.ordinal()] = 48;
         } catch (NoSuchFieldError ex48) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.LAVA.ordinal()] = 49;
         } catch (NoSuchFieldError ex49) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.STATIONARY_LAVA.ordinal()] = 50;
         } catch (NoSuchFieldError ex50) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.COBBLE_WALL.ordinal()] = 51;
         } catch (NoSuchFieldError ex51) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.DAYLIGHT_DETECTOR.ordinal()] = 52;
         } catch (NoSuchFieldError ex52) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.DAYLIGHT_DETECTOR_INVERTED.ordinal()] = 53;
         } catch (NoSuchFieldError ex53) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.ENCHANTMENT_TABLE.ordinal()] = 54;
         } catch (NoSuchFieldError ex54) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.ENDER_CHEST.ordinal()] = 55;
         } catch (NoSuchFieldError ex55) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.CHEST.ordinal()] = 56;
         } catch (NoSuchFieldError ex56) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.TRAPPED_CHEST.ordinal()] = 57;
         } catch (NoSuchFieldError ex57) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.GLASS.ordinal()] = 58;
         } catch (NoSuchFieldError ex58) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.STAINED_GLASS_PANE.ordinal()] = 59;
         } catch (NoSuchFieldError ex59) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.STAINED_GLASS.ordinal()] = 60;
         } catch (NoSuchFieldError ex60) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.THIN_GLASS.ordinal()] = 61;
         } catch (NoSuchFieldError ex61) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.HOPPER.ordinal()] = 62;
         } catch (NoSuchFieldError ex62) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.STEP.ordinal()] = 63;
         } catch (NoSuchFieldError ex63) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.STONE_SLAB2.ordinal()] = 64;
         } catch (NoSuchFieldError ex64) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.WOOD_STEP.ordinal()] = 65;
         } catch (NoSuchFieldError ex65) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.TRAP_DOOR.ordinal()] = 66;
         } catch (NoSuchFieldError ex66) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.IRON_TRAPDOOR.ordinal()] = 67;
         } catch (NoSuchFieldError ex67) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.GOLD_PLATE.ordinal()] = 68;
         } catch (NoSuchFieldError ex68) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.IRON_PLATE.ordinal()] = 69;
         } catch (NoSuchFieldError ex69) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.STONE_PLATE.ordinal()] = 70;
         } catch (NoSuchFieldError ex70) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.WOOD_PLATE.ordinal()] = 71;
         } catch (NoSuchFieldError ex71) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.ENDER_PORTAL_FRAME.ordinal()] = 72;
         } catch (NoSuchFieldError ex72) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.SIGN_POST.ordinal()] = 73;
         } catch (NoSuchFieldError ex73) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.SIGN.ordinal()] = 74;
         } catch (NoSuchFieldError ex74) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.WALL_SIGN.ordinal()] = 75;
         } catch (NoSuchFieldError ex75) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.CACTUS.ordinal()] = 76;
         } catch (NoSuchFieldError ex76) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.MOB_SPAWNER.ordinal()] = 77;
         } catch (NoSuchFieldError ex77) {
         }

         try {
            MATERIAL_SWITCH_TABLE[Material.SLIME_BLOCK.ordinal()] = 78;
         } catch (NoSuchFieldError ex78) {
         }

         SWITCH_TABLE = new int[ToolMiningLevel.values().length];

         try {
            SWITCH_TABLE[ToolMiningLevel.WOOD.ordinal()] = 1;
         } catch (NoSuchFieldError ex79) {
         }

         try {
            SWITCH_TABLE[ToolMiningLevel.GOLD.ordinal()] = 2;
         } catch (NoSuchFieldError ex80) {
         }

         try {
            SWITCH_TABLE[ToolMiningLevel.STONE.ordinal()] = 3;
         } catch (NoSuchFieldError ex81) {
         }

         try {
            SWITCH_TABLE[ToolMiningLevel.IRON.ordinal()] = 4;
         } catch (NoSuchFieldError ex82) {
         }

         try {
            SWITCH_TABLE[ToolMiningLevel.DIAMOND.ordinal()] = 5;
         } catch (NoSuchFieldError ex83) {
         }

         try {
            SWITCH_TABLE[ToolMiningLevel.NA.ordinal()] = 6;
         } catch (NoSuchFieldError ex84) {
         }
      }
   }
}
