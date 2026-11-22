package me.konsolas.aac.util.blocks;

public enum ToolMiningLevel {
   WOOD(1),
   GOLD(1),
   STONE(2),
   IRON(3),
   DIAMOND(4),
   NA(0);

   private int tier;
   private static final ToolMiningLevel[] VALUES = new ToolMiningLevel[]{WOOD, GOLD, STONE, IRON, DIAMOND, NA};

   ToolMiningLevel(int i) {
      this.tier = i;
   }

   public static boolean isRightLevel(ToolMiningLevel toolMiningLevel, ToolMiningLevel toolMiningLevel2) {
      return toolMiningLevel2.tier >= toolMiningLevel.tier;
   }
}
