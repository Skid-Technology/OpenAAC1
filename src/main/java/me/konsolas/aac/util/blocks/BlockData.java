package me.konsolas.aac.util.blocks;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;

public class BlockData {
   private static Map<Material, MaterialSwitch> PROPERTIES = new HashMap<>();

   public static MiningToolCategory getToolCategory(Material material) {
      return PROPERTIES.containsKey(material) ? PROPERTIES.get(material).getToolCategory() : MiningToolCategory.NONE;
   }

   public static ToolMiningLevel getMinimumLevel(Material material) {
      return PROPERTIES.containsKey(material) ? PROPERTIES.get(material).getMinimumLevel() : ToolMiningLevel.NA;
   }

   public static double getHardness(Material material) {
      return PROPERTIES.containsKey(material) ? PROPERTIES.get(material).getHardness() : 0.0;
   }

   public static boolean isFullBlock(Material material) {
      return PROPERTIES.containsKey(material) ? PROPERTIES.get(material).isSolidFullBlock() : true;
   }

   static {
      PROPERTIES.put(Material.BARRIER, new MaterialSwitch(Double.POSITIVE_INFINITY, MiningToolCategory.NONE, ToolMiningLevel.NA));
      PROPERTIES.put(Material.BEDROCK, new MaterialSwitch(Double.POSITIVE_INFINITY, MiningToolCategory.NONE, ToolMiningLevel.NA));
      PROPERTIES.put(Material.COMMAND, new MaterialSwitch(Double.POSITIVE_INFINITY, MiningToolCategory.NONE, ToolMiningLevel.NA));
      PROPERTIES.put(Material.ENDER_PORTAL, new MaterialSwitch(Double.POSITIVE_INFINITY, MiningToolCategory.NONE, ToolMiningLevel.NA));
      PROPERTIES.put(Material.ENDER_PORTAL_FRAME, new MaterialSwitch(Double.POSITIVE_INFINITY, MiningToolCategory.NONE, ToolMiningLevel.NA));
      PROPERTIES.put(Material.AIR, new MaterialSwitch(Double.POSITIVE_INFINITY, MiningToolCategory.NONE, ToolMiningLevel.NA));
      PROPERTIES.put(Material.LAVA, new MaterialSwitch(Double.POSITIVE_INFINITY, MiningToolCategory.NONE, ToolMiningLevel.NA));
      PROPERTIES.put(Material.STATIONARY_LAVA, new MaterialSwitch(Double.POSITIVE_INFINITY, MiningToolCategory.NONE, ToolMiningLevel.NA));
      PROPERTIES.put(Material.WATER, new MaterialSwitch(Double.POSITIVE_INFINITY, MiningToolCategory.NONE, ToolMiningLevel.NA));
      PROPERTIES.put(Material.STATIONARY_WATER, new MaterialSwitch(Double.POSITIVE_INFINITY, MiningToolCategory.NONE, ToolMiningLevel.NA));
      PROPERTIES.put(Material.OBSIDIAN, new MaterialSwitch(50.0, MiningToolCategory.PICKAXE, ToolMiningLevel.DIAMOND));
      PROPERTIES.put(Material.ENDER_CHEST, new MaterialSwitch(22.5, MiningToolCategory.PICKAXE, ToolMiningLevel.DIAMOND));
      PROPERTIES.put(Material.ANVIL, new MaterialSwitch(5.0, MiningToolCategory.PICKAXE, ToolMiningLevel.WOOD));
      PROPERTIES.put(Material.COAL_BLOCK, new MaterialSwitch(5.0, MiningToolCategory.PICKAXE, ToolMiningLevel.WOOD));
      PROPERTIES.put(Material.DIAMOND_BLOCK, new MaterialSwitch(5.0, MiningToolCategory.PICKAXE, ToolMiningLevel.IRON));
      PROPERTIES.put(Material.EMERALD_BLOCK, new MaterialSwitch(5.0, MiningToolCategory.PICKAXE, ToolMiningLevel.IRON));
      PROPERTIES.put(Material.IRON_BLOCK, new MaterialSwitch(5.0, MiningToolCategory.PICKAXE, ToolMiningLevel.STONE));
      PROPERTIES.put(Material.REDSTONE_BLOCK, new MaterialSwitch(5.0, MiningToolCategory.PICKAXE, ToolMiningLevel.WOOD));
      PROPERTIES.put(Material.ENCHANTMENT_TABLE, new MaterialSwitch(5.0, MiningToolCategory.PICKAXE, ToolMiningLevel.WOOD));
      PROPERTIES.put(Material.IRON_FENCE, new MaterialSwitch(5.0, MiningToolCategory.PICKAXE, ToolMiningLevel.WOOD));
      PROPERTIES.put(Material.IRON_DOOR_BLOCK, new MaterialSwitch(5.0, MiningToolCategory.PICKAXE, ToolMiningLevel.WOOD));
      PROPERTIES.put(Material.IRON_TRAPDOOR, new MaterialSwitch(5.0, MiningToolCategory.PICKAXE, ToolMiningLevel.WOOD));
      PROPERTIES.put(Material.MOB_SPAWNER, new MaterialSwitch(5.0, MiningToolCategory.PICKAXE, ToolMiningLevel.WOOD));
      PROPERTIES.put(Material.WEB, new MaterialSwitch(4.0, MiningToolCategory.SHEARS, ToolMiningLevel.NA));
      PROPERTIES.put(Material.DISPENSER, new MaterialSwitch(3.5, MiningToolCategory.PICKAXE, ToolMiningLevel.WOOD));
      PROPERTIES.put(Material.DROPPER, new MaterialSwitch(3.5, MiningToolCategory.PICKAXE, ToolMiningLevel.WOOD));
      PROPERTIES.put(Material.FURNACE, new MaterialSwitch(3.5, MiningToolCategory.PICKAXE, ToolMiningLevel.WOOD));
      PROPERTIES.put(Material.BEACON, new MaterialSwitch(3.0, MiningToolCategory.NONE, ToolMiningLevel.NA));
      PROPERTIES.put(Material.GOLD_BLOCK, new MaterialSwitch(3.0, MiningToolCategory.PICKAXE, ToolMiningLevel.IRON));
      PROPERTIES.put(Material.COAL_ORE, new MaterialSwitch(3.0, MiningToolCategory.PICKAXE, ToolMiningLevel.WOOD));
      PROPERTIES.put(Material.DRAGON_EGG, new MaterialSwitch(3.0, MiningToolCategory.NONE, ToolMiningLevel.NA));
      PROPERTIES.put(Material.DIAMOND_ORE, new MaterialSwitch(3.0, MiningToolCategory.PICKAXE, ToolMiningLevel.IRON));
      PROPERTIES.put(Material.EMERALD_ORE, new MaterialSwitch(3.0, MiningToolCategory.PICKAXE, ToolMiningLevel.IRON));
      PROPERTIES.put(Material.ENDER_STONE, new MaterialSwitch(3.0, MiningToolCategory.PICKAXE, ToolMiningLevel.WOOD));
      PROPERTIES.put(Material.GOLD_ORE, new MaterialSwitch(3.0, MiningToolCategory.PICKAXE, ToolMiningLevel.IRON));
      PROPERTIES.put(Material.HOPPER, new MaterialSwitch(3.0, MiningToolCategory.PICKAXE, ToolMiningLevel.WOOD));
      PROPERTIES.put(Material.IRON_ORE, new MaterialSwitch(3.0, MiningToolCategory.PICKAXE, ToolMiningLevel.STONE));
      PROPERTIES.put(Material.LAPIS_BLOCK, new MaterialSwitch(3.0, MiningToolCategory.PICKAXE, ToolMiningLevel.STONE));
      PROPERTIES.put(Material.LAPIS_ORE, new MaterialSwitch(3.0, MiningToolCategory.PICKAXE, ToolMiningLevel.STONE));
      PROPERTIES.put(Material.QUARTZ_ORE, new MaterialSwitch(3.0, MiningToolCategory.PICKAXE, ToolMiningLevel.WOOD));
      PROPERTIES.put(Material.REDSTONE_ORE, new MaterialSwitch(3.0, MiningToolCategory.PICKAXE, ToolMiningLevel.IRON));
      PROPERTIES.put(Material.TRAP_DOOR, new MaterialSwitch(3.0, MiningToolCategory.AXE, ToolMiningLevel.WOOD, true));
      PROPERTIES.put(Material.WOODEN_DOOR, new MaterialSwitch(3.0, MiningToolCategory.AXE, ToolMiningLevel.WOOD, true));
      PROPERTIES.put(Material.ACACIA_DOOR, new MaterialSwitch(3.0, MiningToolCategory.AXE, ToolMiningLevel.WOOD, true));
      PROPERTIES.put(Material.SPRUCE_DOOR, new MaterialSwitch(3.0, MiningToolCategory.AXE, ToolMiningLevel.WOOD, true));
      PROPERTIES.put(Material.BIRCH_DOOR, new MaterialSwitch(3.0, MiningToolCategory.AXE, ToolMiningLevel.WOOD, true));
      PROPERTIES.put(Material.JUNGLE_DOOR, new MaterialSwitch(3.0, MiningToolCategory.AXE, ToolMiningLevel.WOOD, true));
      PROPERTIES.put(Material.DARK_OAK_DOOR, new MaterialSwitch(3.0, MiningToolCategory.AXE, ToolMiningLevel.WOOD, true));
      PROPERTIES.put(Material.CHEST, new MaterialSwitch(2.5, MiningToolCategory.AXE, ToolMiningLevel.WOOD, true));
      PROPERTIES.put(Material.TRAPPED_CHEST, new MaterialSwitch(2.5, MiningToolCategory.AXE, ToolMiningLevel.WOOD, true));
      PROPERTIES.put(Material.WORKBENCH, new MaterialSwitch(2.5, MiningToolCategory.AXE, ToolMiningLevel.WOOD, true));
      PROPERTIES.put(Material.BRICK_STAIRS, new MaterialSwitch(2.0, MiningToolCategory.PICKAXE, ToolMiningLevel.WOOD));
      PROPERTIES.put(Material.BRICK, new MaterialSwitch(2.0, MiningToolCategory.PICKAXE, ToolMiningLevel.WOOD));
      PROPERTIES.put(Material.CAULDRON, new MaterialSwitch(2.0, MiningToolCategory.PICKAXE, ToolMiningLevel.WOOD));
      PROPERTIES.put(Material.COBBLESTONE, new MaterialSwitch(2.0, MiningToolCategory.PICKAXE, ToolMiningLevel.WOOD));
      PROPERTIES.put(Material.COBBLESTONE_STAIRS, new MaterialSwitch(2.0, MiningToolCategory.PICKAXE, ToolMiningLevel.WOOD));
      PROPERTIES.put(Material.COBBLE_WALL, new MaterialSwitch(2.0, MiningToolCategory.PICKAXE, ToolMiningLevel.WOOD));
      PROPERTIES.put(Material.FENCE, new MaterialSwitch(2.0, MiningToolCategory.AXE, ToolMiningLevel.WOOD, true));
      PROPERTIES.put(Material.ACACIA_FENCE, new MaterialSwitch(2.0, MiningToolCategory.AXE, ToolMiningLevel.WOOD, true));
      PROPERTIES.put(Material.SPRUCE_FENCE, new MaterialSwitch(2.0, MiningToolCategory.AXE, ToolMiningLevel.WOOD, true));
      PROPERTIES.put(Material.BIRCH_FENCE, new MaterialSwitch(2.0, MiningToolCategory.AXE, ToolMiningLevel.WOOD, true));
      PROPERTIES.put(Material.JUNGLE_FENCE, new MaterialSwitch(2.0, MiningToolCategory.AXE, ToolMiningLevel.WOOD, true));
      PROPERTIES.put(Material.DARK_OAK_FENCE, new MaterialSwitch(2.0, MiningToolCategory.AXE, ToolMiningLevel.WOOD, true));
      PROPERTIES.put(Material.FENCE_GATE, new MaterialSwitch(2.0, MiningToolCategory.AXE, ToolMiningLevel.WOOD, true));
      PROPERTIES.put(Material.ACACIA_FENCE_GATE, new MaterialSwitch(2.0, MiningToolCategory.AXE, ToolMiningLevel.WOOD, true));
      PROPERTIES.put(Material.SPRUCE_FENCE_GATE, new MaterialSwitch(2.0, MiningToolCategory.AXE, ToolMiningLevel.WOOD, true));
      PROPERTIES.put(Material.BIRCH_FENCE_GATE, new MaterialSwitch(2.0, MiningToolCategory.AXE, ToolMiningLevel.WOOD, true));
      PROPERTIES.put(Material.JUNGLE_FENCE_GATE, new MaterialSwitch(2.0, MiningToolCategory.AXE, ToolMiningLevel.WOOD, true));
      PROPERTIES.put(Material.DARK_OAK_FENCE_GATE, new MaterialSwitch(2.0, MiningToolCategory.AXE, ToolMiningLevel.WOOD, true));
      PROPERTIES.put(Material.JUKEBOX, new MaterialSwitch(2.0, MiningToolCategory.AXE, ToolMiningLevel.WOOD, true));
      PROPERTIES.put(Material.MOSSY_COBBLESTONE, new MaterialSwitch(2.0, MiningToolCategory.PICKAXE, ToolMiningLevel.WOOD));
      PROPERTIES.put(Material.NETHER_BRICK, new MaterialSwitch(2.0, MiningToolCategory.PICKAXE, ToolMiningLevel.WOOD));
      PROPERTIES.put(Material.NETHER_BRICK_STAIRS, new MaterialSwitch(2.0, MiningToolCategory.PICKAXE, ToolMiningLevel.WOOD));
      PROPERTIES.put(Material.NETHER_FENCE, new MaterialSwitch(2.0, MiningToolCategory.PICKAXE, ToolMiningLevel.WOOD));
      PROPERTIES.put(Material.STEP, new MaterialSwitch(2.0, MiningToolCategory.PICKAXE, ToolMiningLevel.WOOD));
      PROPERTIES.put(Material.LOG, new MaterialSwitch(2.0, MiningToolCategory.AXE, ToolMiningLevel.WOOD, true));
      PROPERTIES.put(Material.LOG_2, new MaterialSwitch(2.0, MiningToolCategory.AXE, ToolMiningLevel.WOOD, true));
      PROPERTIES.put(Material.WOOD, new MaterialSwitch(2.0, MiningToolCategory.AXE, ToolMiningLevel.WOOD, true));
      PROPERTIES.put(Material.STONE_SLAB2, new MaterialSwitch(2.0, MiningToolCategory.PICKAXE, ToolMiningLevel.WOOD));
      PROPERTIES.put(Material.DOUBLE_STONE_SLAB2, new MaterialSwitch(2.0, MiningToolCategory.PICKAXE, ToolMiningLevel.WOOD));
      PROPERTIES.put(Material.DOUBLE_STEP, new MaterialSwitch(2.0, MiningToolCategory.PICKAXE, ToolMiningLevel.WOOD));
      PROPERTIES.put(Material.WOOD_STEP, new MaterialSwitch(2.0, MiningToolCategory.AXE, ToolMiningLevel.WOOD, true));
      PROPERTIES.put(Material.WOOD_DOUBLE_STEP, new MaterialSwitch(2.0, MiningToolCategory.AXE, ToolMiningLevel.WOOD, true));
      PROPERTIES.put(Material.WOOD_STAIRS, new MaterialSwitch(2.0, MiningToolCategory.AXE, ToolMiningLevel.WOOD, true));
      PROPERTIES.put(Material.ACACIA_STAIRS, new MaterialSwitch(2.0, MiningToolCategory.AXE, ToolMiningLevel.WOOD, true));
      PROPERTIES.put(Material.SPRUCE_WOOD_STAIRS, new MaterialSwitch(2.0, MiningToolCategory.AXE, ToolMiningLevel.WOOD, true));
      PROPERTIES.put(Material.BIRCH_WOOD_STAIRS, new MaterialSwitch(2.0, MiningToolCategory.AXE, ToolMiningLevel.WOOD, true));
      PROPERTIES.put(Material.JUNGLE_WOOD_STAIRS, new MaterialSwitch(2.0, MiningToolCategory.AXE, ToolMiningLevel.WOOD, true));
      PROPERTIES.put(Material.DARK_OAK_STAIRS, new MaterialSwitch(2.0, MiningToolCategory.AXE, ToolMiningLevel.WOOD, true));
      PROPERTIES.put(Material.STONE, new MaterialSwitch(1.5, MiningToolCategory.PICKAXE, ToolMiningLevel.WOOD));
      PROPERTIES.put(Material.PRISMARINE, new MaterialSwitch(1.5, MiningToolCategory.PICKAXE, ToolMiningLevel.WOOD));
      PROPERTIES.put(Material.SMOOTH_BRICK, new MaterialSwitch(1.5, MiningToolCategory.PICKAXE, ToolMiningLevel.WOOD));
      PROPERTIES.put(Material.SMOOTH_STAIRS, new MaterialSwitch(1.5, MiningToolCategory.PICKAXE, ToolMiningLevel.WOOD));
      PROPERTIES.put(Material.HARD_CLAY, new MaterialSwitch(1.25, MiningToolCategory.PICKAXE, ToolMiningLevel.WOOD));
      PROPERTIES.put(Material.STAINED_CLAY, new MaterialSwitch(1.25, MiningToolCategory.PICKAXE, ToolMiningLevel.WOOD));
      PROPERTIES.put(Material.BANNER, new MaterialSwitch(1.0, MiningToolCategory.AXE, ToolMiningLevel.WOOD, true));
      PROPERTIES.put(Material.JACK_O_LANTERN, new MaterialSwitch(1.0, MiningToolCategory.AXE, ToolMiningLevel.WOOD, true));
      PROPERTIES.put(Material.MELON_BLOCK, new MaterialSwitch(1.0, MiningToolCategory.AXE, ToolMiningLevel.WOOD, true));
      PROPERTIES.put(Material.SKULL, new MaterialSwitch(1.0, MiningToolCategory.NONE, ToolMiningLevel.NA, true));
      PROPERTIES.put(Material.PUMPKIN, new MaterialSwitch(1.0, MiningToolCategory.AXE, ToolMiningLevel.WOOD, true));
      PROPERTIES.put(Material.SIGN_POST, new MaterialSwitch(1.0, MiningToolCategory.AXE, ToolMiningLevel.WOOD, true));
      PROPERTIES.put(Material.WALL_SIGN, new MaterialSwitch(1.0, MiningToolCategory.AXE, ToolMiningLevel.WOOD, true));
      PROPERTIES.put(Material.QUARTZ_BLOCK, new MaterialSwitch(0.8, MiningToolCategory.PICKAXE, ToolMiningLevel.WOOD));
      PROPERTIES.put(Material.NOTE_BLOCK, new MaterialSwitch(0.8, MiningToolCategory.AXE, ToolMiningLevel.WOOD, true));
      PROPERTIES.put(Material.QUARTZ_STAIRS, new MaterialSwitch(0.8, MiningToolCategory.PICKAXE, ToolMiningLevel.WOOD));
      PROPERTIES.put(Material.RED_SANDSTONE, new MaterialSwitch(0.8, MiningToolCategory.PICKAXE, ToolMiningLevel.WOOD));
      PROPERTIES.put(Material.RED_SANDSTONE_STAIRS, new MaterialSwitch(0.8, MiningToolCategory.PICKAXE, ToolMiningLevel.WOOD));
      PROPERTIES.put(Material.SANDSTONE, new MaterialSwitch(0.8, MiningToolCategory.PICKAXE, ToolMiningLevel.WOOD));
      PROPERTIES.put(Material.SANDSTONE_STAIRS, new MaterialSwitch(0.8, MiningToolCategory.PICKAXE, ToolMiningLevel.WOOD));
      PROPERTIES.put(Material.WOOL, new MaterialSwitch(0.8, MiningToolCategory.SHEARS, ToolMiningLevel.NA, true));
      PROPERTIES.put(Material.MONSTER_EGG, new MaterialSwitch(0.75, MiningToolCategory.NONE, ToolMiningLevel.NA));
      PROPERTIES.put(Material.RAILS, new MaterialSwitch(0.7, MiningToolCategory.PICKAXE, ToolMiningLevel.WOOD, true));
      PROPERTIES.put(Material.ACTIVATOR_RAIL, new MaterialSwitch(0.7, MiningToolCategory.PICKAXE, ToolMiningLevel.WOOD, true));
      PROPERTIES.put(Material.DETECTOR_RAIL, new MaterialSwitch(0.7, MiningToolCategory.PICKAXE, ToolMiningLevel.WOOD, true));
      PROPERTIES.put(Material.POWERED_RAIL, new MaterialSwitch(0.7, MiningToolCategory.PICKAXE, ToolMiningLevel.WOOD, true));
      PROPERTIES.put(Material.CLAY, new MaterialSwitch(0.6, MiningToolCategory.SHOVEL, ToolMiningLevel.WOOD, true));
      PROPERTIES.put(Material.SOIL, new MaterialSwitch(0.6, MiningToolCategory.SHOVEL, ToolMiningLevel.WOOD, true));
      PROPERTIES.put(Material.GRASS, new MaterialSwitch(0.6, MiningToolCategory.SHOVEL, ToolMiningLevel.WOOD, true));
      PROPERTIES.put(Material.GRAVEL, new MaterialSwitch(0.6, MiningToolCategory.SHOVEL, ToolMiningLevel.WOOD, true));
      PROPERTIES.put(Material.MYCEL, new MaterialSwitch(0.6, MiningToolCategory.SHOVEL, ToolMiningLevel.WOOD, true));
      PROPERTIES.put(Material.SPONGE, new MaterialSwitch(0.6, MiningToolCategory.NONE, ToolMiningLevel.NA, true));
      PROPERTIES.put(Material.BREWING_STAND, new MaterialSwitch(0.5, MiningToolCategory.PICKAXE, ToolMiningLevel.WOOD));
      PROPERTIES.put(Material.STONE_BUTTON, new MaterialSwitch(0.5, MiningToolCategory.NONE, ToolMiningLevel.NA, true));
      PROPERTIES.put(Material.CAKE, new MaterialSwitch(0.5, MiningToolCategory.NONE, ToolMiningLevel.NA, true));
      PROPERTIES.put(Material.DIRT, new MaterialSwitch(0.5, MiningToolCategory.SHOVEL, ToolMiningLevel.NA, true));
      PROPERTIES.put(Material.HAY_BLOCK, new MaterialSwitch(0.5, MiningToolCategory.NONE, ToolMiningLevel.NA, true));
      PROPERTIES.put(Material.ICE, new MaterialSwitch(0.5, MiningToolCategory.PICKAXE, ToolMiningLevel.WOOD, true));
      PROPERTIES.put(Material.LEVER, new MaterialSwitch(0.5, MiningToolCategory.NONE, ToolMiningLevel.NA, true));
      PROPERTIES.put(Material.PACKED_ICE, new MaterialSwitch(0.5, MiningToolCategory.PICKAXE, ToolMiningLevel.WOOD, true));
      PROPERTIES.put(Material.PISTON_BASE, new MaterialSwitch(0.5, MiningToolCategory.NONE, ToolMiningLevel.NA, true));
      PROPERTIES.put(Material.PISTON_EXTENSION, new MaterialSwitch(0.5, MiningToolCategory.NONE, ToolMiningLevel.NA, true));
      PROPERTIES.put(Material.PISTON_MOVING_PIECE, new MaterialSwitch(0.5, MiningToolCategory.NONE, ToolMiningLevel.NA, true));
      PROPERTIES.put(Material.PISTON_STICKY_BASE, new MaterialSwitch(0.5, MiningToolCategory.NONE, ToolMiningLevel.NA, true));
      PROPERTIES.put(Material.SAND, new MaterialSwitch(0.5, MiningToolCategory.SHOVEL, ToolMiningLevel.WOOD, true));
      PROPERTIES.put(Material.SOUL_SAND, new MaterialSwitch(0.5, MiningToolCategory.SHOVEL, ToolMiningLevel.WOOD, true));
      PROPERTIES.put(Material.GOLD_PLATE, new MaterialSwitch(0.5, MiningToolCategory.PICKAXE, ToolMiningLevel.WOOD));
      PROPERTIES.put(Material.IRON_PLATE, new MaterialSwitch(0.5, MiningToolCategory.PICKAXE, ToolMiningLevel.WOOD));
      PROPERTIES.put(Material.STONE_PLATE, new MaterialSwitch(0.5, MiningToolCategory.PICKAXE, ToolMiningLevel.WOOD));
      PROPERTIES.put(Material.WOOD_PLATE, new MaterialSwitch(0.5, MiningToolCategory.AXE, ToolMiningLevel.WOOD));
      PROPERTIES.put(Material.CACTUS, new MaterialSwitch(0.4, MiningToolCategory.NONE, ToolMiningLevel.NA, true));
      PROPERTIES.put(Material.LADDER, new MaterialSwitch(0.4, MiningToolCategory.AXE, ToolMiningLevel.WOOD, true));
      PROPERTIES.put(Material.NETHERRACK, new MaterialSwitch(0.4, MiningToolCategory.PICKAXE, ToolMiningLevel.WOOD));
      PROPERTIES.put(Material.GLASS, new MaterialSwitch(0.3, MiningToolCategory.NONE, ToolMiningLevel.NA));
      PROPERTIES.put(Material.STAINED_GLASS_PANE, new MaterialSwitch(0.3, MiningToolCategory.NONE, ToolMiningLevel.NA));
      PROPERTIES.put(Material.STAINED_GLASS, new MaterialSwitch(0.3, MiningToolCategory.NONE, ToolMiningLevel.NA));
      PROPERTIES.put(Material.THIN_GLASS, new MaterialSwitch(0.3, MiningToolCategory.NONE, ToolMiningLevel.NA));
      PROPERTIES.put(Material.GLOWSTONE, new MaterialSwitch(0.3, MiningToolCategory.NONE, ToolMiningLevel.NA, true));
      PROPERTIES.put(Material.REDSTONE_LAMP_ON, new MaterialSwitch(0.3, MiningToolCategory.NONE, ToolMiningLevel.NA, true));
      PROPERTIES.put(Material.REDSTONE_LAMP_OFF, new MaterialSwitch(0.3, MiningToolCategory.NONE, ToolMiningLevel.NA, true));
      PROPERTIES.put(Material.SEA_LANTERN, new MaterialSwitch(0.3, MiningToolCategory.NONE, ToolMiningLevel.NA, true));
      PROPERTIES.put(Material.BED, new MaterialSwitch(0.2, MiningToolCategory.NONE, ToolMiningLevel.NA, true));
      PROPERTIES.put(Material.COCOA, new MaterialSwitch(0.2, MiningToolCategory.AXE, ToolMiningLevel.WOOD, true));
      PROPERTIES.put(Material.DAYLIGHT_DETECTOR, new MaterialSwitch(0.2, MiningToolCategory.AXE, ToolMiningLevel.WOOD, true));
      PROPERTIES.put(Material.DAYLIGHT_DETECTOR_INVERTED, new MaterialSwitch(0.2, MiningToolCategory.AXE, ToolMiningLevel.WOOD, true));
      PROPERTIES.put(Material.HUGE_MUSHROOM_1, new MaterialSwitch(0.1, MiningToolCategory.AXE, ToolMiningLevel.WOOD, true));
      PROPERTIES.put(Material.HUGE_MUSHROOM_2, new MaterialSwitch(0.1, MiningToolCategory.AXE, ToolMiningLevel.WOOD, true));
      PROPERTIES.put(Material.SNOW_BLOCK, new MaterialSwitch(0.1, MiningToolCategory.SHOVEL, ToolMiningLevel.WOOD, true));
      PROPERTIES.put(Material.VINE, new MaterialSwitch(0.1, MiningToolCategory.AXE, ToolMiningLevel.WOOD, true));
      PROPERTIES.put(Material.CARPET, new MaterialSwitch(0.1, MiningToolCategory.NONE, ToolMiningLevel.NA, true));
   }

   private static class MaterialSwitch {
      private double hardness;
      private MiningToolCategory toolCategory;
      private ToolMiningLevel minimumLevel;
      private boolean solidFullBlock;

      public MaterialSwitch(double d, MiningToolCategory miningToolCategory, ToolMiningLevel toolMiningLevel, boolean flag) {
         this.hardness = d;
         this.toolCategory = miningToolCategory;
         this.minimumLevel = toolMiningLevel;
         this.solidFullBlock = flag;
      }

      public MaterialSwitch(double d, MiningToolCategory miningToolCategory, ToolMiningLevel toolMiningLevel) {
         this.hardness = d;
         this.toolCategory = miningToolCategory;
         this.minimumLevel = toolMiningLevel;
         this.solidFullBlock = false;
      }

      public double getHardness() {
         return this.hardness;
      }

      public MiningToolCategory getToolCategory() {
         return this.toolCategory;
      }

      public ToolMiningLevel getMinimumLevel() {
         return this.minimumLevel;
      }

      public boolean isSolidFullBlock() {
         return this.solidFullBlock;
      }
   }
}
