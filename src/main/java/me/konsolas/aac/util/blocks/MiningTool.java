package me.konsolas.aac.util.blocks;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class MiningTool {
   private MiningToolCategory category;
   private ToolMiningLevel level;
   private int efficiencyLevel;

   public MiningTool(MiningToolCategory miningToolCategory, ToolMiningLevel toolMiningLevel, int i) {
      this.category = miningToolCategory;
      this.level = toolMiningLevel;
      this.efficiencyLevel = i;
   }

   public MiningTool(ItemStack item) {
      if (item == null) {
         item = new ItemStack(Material.AIR);
      }

      switch (MaterialSwitch.SWITCH_TABLE[item.getType().ordinal()]) {
         case 1:
         case 2:
         case 3:
         case 4:
         case 5:
            this.category = MiningToolCategory.PICKAXE;
            break;
         case 6:
         case 7:
         case 8:
         case 9:
         case 10:
            this.category = MiningToolCategory.AXE;
            break;
         case 11:
         case 12:
         case 13:
         case 14:
         case 15:
            this.category = MiningToolCategory.SHOVEL;
            break;
         case 16:
         case 17:
         case 18:
         case 19:
         case 20:
            this.category = MiningToolCategory.SHEARS;
            break;
         case 21:
            this.category = MiningToolCategory.SHEARS;
            break;
         default:
            this.category = MiningToolCategory.NONE;
      }

      switch (MaterialSwitch.SWITCH_TABLE[item.getType().ordinal()]) {
         case 1:
         case 6:
         case 11:
         case 16:
            this.level = ToolMiningLevel.WOOD;
            break;
         case 2:
         case 7:
         case 12:
         case 17:
            this.level = ToolMiningLevel.GOLD;
            break;
         case 3:
         case 8:
         case 13:
         case 18:
            this.level = ToolMiningLevel.STONE;
            break;
         case 4:
         case 9:
         case 14:
         case 19:
            this.level = ToolMiningLevel.IRON;
            break;
         case 5:
         case 10:
         case 15:
         case 20:
            this.level = ToolMiningLevel.DIAMOND;
            break;
         default:
            this.level = ToolMiningLevel.NA;
      }

      this.efficiencyLevel = item.getEnchantmentLevel(Enchantment.DIG_SPEED);
   }

   public MiningToolCategory getCategory() {
      return this.category;
   }

   public ToolMiningLevel getLevel() {
      return this.level;
   }

   public int getEfficiencyLevel() {
      return this.efficiencyLevel;
   }

   class MaterialSwitch {
      static final int[] SWITCH_TABLE = new int[Material.values().length];

      static {
         try {
            SWITCH_TABLE[Material.WOOD_PICKAXE.ordinal()] = 1;
         } catch (NoSuchFieldError ex) {
         }

         try {
            SWITCH_TABLE[Material.GOLD_PICKAXE.ordinal()] = 2;
         } catch (NoSuchFieldError ex2) {
         }

         try {
            SWITCH_TABLE[Material.STONE_PICKAXE.ordinal()] = 3;
         } catch (NoSuchFieldError ex3) {
         }

         try {
            SWITCH_TABLE[Material.IRON_PICKAXE.ordinal()] = 4;
         } catch (NoSuchFieldError ex4) {
         }

         try {
            SWITCH_TABLE[Material.DIAMOND_PICKAXE.ordinal()] = 5;
         } catch (NoSuchFieldError ex5) {
         }

         try {
            SWITCH_TABLE[Material.WOOD_AXE.ordinal()] = 6;
         } catch (NoSuchFieldError ex6) {
         }

         try {
            SWITCH_TABLE[Material.GOLD_AXE.ordinal()] = 7;
         } catch (NoSuchFieldError ex7) {
         }

         try {
            SWITCH_TABLE[Material.STONE_AXE.ordinal()] = 8;
         } catch (NoSuchFieldError ex8) {
         }

         try {
            SWITCH_TABLE[Material.IRON_AXE.ordinal()] = 9;
         } catch (NoSuchFieldError ex9) {
         }

         try {
            SWITCH_TABLE[Material.DIAMOND_AXE.ordinal()] = 10;
         } catch (NoSuchFieldError ex10) {
         }

         try {
            SWITCH_TABLE[Material.WOOD_SPADE.ordinal()] = 11;
         } catch (NoSuchFieldError ex11) {
         }

         try {
            SWITCH_TABLE[Material.GOLD_SPADE.ordinal()] = 12;
         } catch (NoSuchFieldError ex12) {
         }

         try {
            SWITCH_TABLE[Material.STONE_SPADE.ordinal()] = 13;
         } catch (NoSuchFieldError ex13) {
         }

         try {
            SWITCH_TABLE[Material.IRON_SPADE.ordinal()] = 14;
         } catch (NoSuchFieldError ex14) {
         }

         try {
            SWITCH_TABLE[Material.DIAMOND_SPADE.ordinal()] = 15;
         } catch (NoSuchFieldError ex15) {
         }

         try {
            SWITCH_TABLE[Material.WOOD_SWORD.ordinal()] = 16;
         } catch (NoSuchFieldError ex16) {
         }

         try {
            SWITCH_TABLE[Material.GOLD_SWORD.ordinal()] = 17;
         } catch (NoSuchFieldError ex17) {
         }

         try {
            SWITCH_TABLE[Material.STONE_SWORD.ordinal()] = 18;
         } catch (NoSuchFieldError ex18) {
         }

         try {
            SWITCH_TABLE[Material.IRON_SWORD.ordinal()] = 19;
         } catch (NoSuchFieldError ex19) {
         }

         try {
            SWITCH_TABLE[Material.DIAMOND_SWORD.ordinal()] = 20;
         } catch (NoSuchFieldError ex20) {
         }

         try {
            SWITCH_TABLE[Material.SHEARS.ordinal()] = 21;
         } catch (NoSuchFieldError ex21) {
         }
      }
   }
}
