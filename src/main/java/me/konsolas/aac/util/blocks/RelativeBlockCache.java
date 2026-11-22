package me.konsolas.aac.util.blocks;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

public class RelativeBlockCache {
   private double x;
   private double y;
   private double z;
   private World world;
   private final Map<BlockCoordinates, Block> cachedBlocks = new HashMap<>();

   public RelativeBlockCache(Location location) {
      this.x = location.getX();
      this.y = location.getY();
      this.z = location.getZ();
      this.world = location.getWorld();
   }

   public Block getRelativeBlock(double d, double d2, double d3) {
      return this.getBlockAt((int)Math.floor(this.x + d), (int)Math.floor(this.y + d2), (int)Math.floor(this.z + d3));
   }

   public Block getSubtractedBlock(double d, double d2, double d3) {
      return this.getBlockAt((int)Math.floor(this.x - d), (int)Math.floor(this.y - d2), (int)Math.floor(this.z - d3));
   }

   public Material getRelativeMaterial(double d, double d2, double d3) {
      return this.getRelativeBlock(d, d2, d3).getType();
   }

   public Material getSubtractedMaterial(double d, double d2, double d3) {
      return this.getSubtractedBlock(d, d2, d3).getType();
   }

   private Block getBlockAt(int i, int i2, int i3) {
      BlockCoordinates blockCoordinates = new BlockCoordinates(i, i2, i3);
      Block block = this.cachedBlocks.get(blockCoordinates);
      if (block != null) {
         return block;
      }

      Block block2 = this.world.getBlockAt(i, i2, i3);
      this.cachedBlocks.put(blockCoordinates, block2);
      return block2;
   }

   public Block getCenterBlock() {
      return this.getBlockAt((int)this.x, (int)this.y, (int)this.z);
   }

   private static class BlockCoordinates {
      private final int x;
      private final int y;
      private final int z;

      public BlockCoordinates(int i, int i2, int i3) {
         this.x = i;
         this.y = i2;
         this.z = i3;
      }

      public int getX() {
         return this.x;
      }

      public int getY() {
         return this.y;
      }

      public int getZ() {
         return this.z;
      }

      @Override
      public boolean equals(Object obj) {
         if (obj == this) {
            return true;
         }

         if (!(obj instanceof BlockCoordinates)) {
            return false;
         }

         BlockCoordinates blockCoordinates = (BlockCoordinates)obj;
         return blockCoordinates.x == this.x && blockCoordinates.y == this.y && blockCoordinates.z == this.z;
      }

      @Override
      public int hashCode() {
         int value = 17;
         value = 31 * value + this.x;
         value = 31 * value + this.y;
         return 31 * value + this.z;
      }
   }
}
