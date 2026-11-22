package me.konsolas.aac.util.blocks;

import java.util.HashSet;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class BlockCollisionChecks {

   public static boolean isSolidGroundBlock(Block block) {
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
            return true;
         default:
            return false;
      }
   }

   public static boolean isStandableBlock(Block block) {
      if (block.getRelative(BlockFace.UP).getType() == Material.CARPET) {
         return true;
      }

      if (block.getType().isBlock() && block.getType() != Material.AIR) {
         switch (MaterialSwitch.SWITCH_TABLE[block.getType().ordinal()]) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
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
            case 110:
            case 111:
            case 112:
            case 113:
            case 114:
            case 115:
            case 116:
            case 117:
            case 118:
            case 119:
            case 120:
            case 121:
            case 122:
            case 123:
            case 124:
            case 125:
            case 126:
            case 127:
            case 128:
            case 129:
            case 130:
            case 131:
            case 132:
            case 133:
            case 134:
            case 135:
            case 136:
            case 137:
            case 138:
            case 139:
            case 140:
            case 141:
            case 142:
            case 143:
            case 144:
            case 145:
            case 146:
            case 147:
            case 148:
               return true;
            case 8:
            case 50:
            default:
               return false;
         }
      } else {
         return false;
      }
   }

   public static boolean isFullOrHalfBlock(Block block) {
      switch (MaterialSwitch.SWITCH_TABLE[block.getType().ordinal()]) {
         case 1:
         case 2:
         case 3:
         case 4:
         case 5:
         case 6:
         case 7:
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
         case 110:
         case 111:
         case 112:
         case 113:
         case 114:
         case 115:
         case 116:
         case 117:
         case 118:
         case 119:
         case 120:
         case 121:
         case 122:
         case 123:
         case 124:
         case 125:
         case 126:
         case 127:
         case 128:
         case 129:
         case 130:
         case 131:
         case 132:
         case 133:
         case 134:
         case 135:
         case 136:
         case 137:
         case 138:
         case 139:
         case 140:
         case 141:
         case 142:
         case 143:
         case 146:
         case 147:
         case 148:
            return true;
         case 8:
         case 50:
         case 144:
         case 145:
         default:
            return false;
      }
   }

   public static boolean isStepable(Block block) {
      return isWalkableSurface(block) && isStandableBlock(block);
   }

   public static boolean isLava(Block block) {
      return block.getType() == Material.LAVA
         || block.getType() == Material.STATIONARY_LAVA
         || block.getType() == Material.STATIONARY_WATER
         || block.getType() == Material.WATER
         || isStepable(block);
   }

   public static boolean isSlab(Material material) {
      if (material.equals(Material.STEP)) {
         return true;
      } else if (material.equals(Material.WOOD_STEP)) {
         return true;
      } else if (material.equals(Material.DAYLIGHT_DETECTOR)) {
         return true;
      } else if (material.equals(Material.STONE_SLAB2)) {
         return true;
      } else {
         return material.equals(Material.WATER_LILY) ? true : material.equals(Material.SKULL);
      }
   }

   public static boolean isStair(Material material) {
      return material == Material.ACACIA_STAIRS
         || material == Material.BIRCH_WOOD_STAIRS
         || material == Material.BRICK_STAIRS
         || material == Material.COBBLESTONE_STAIRS
         || material == Material.DARK_OAK_STAIRS
         || material == Material.JUNGLE_WOOD_STAIRS
         || material == Material.NETHER_BRICK_STAIRS
         || material == Material.QUARTZ_STAIRS
         || material == Material.RED_SANDSTONE_STAIRS
         || material == Material.SANDSTONE_STAIRS
         || material == Material.SMOOTH_STAIRS
         || material == Material.SPRUCE_WOOD_STAIRS
         || material == Material.WOOD_STAIRS;
   }

   public static boolean isSlabOrStair(Material material) {
      return isSlab(material) || isStair(material);
   }

   public static boolean isFenceOrGate(Material material) {
      return material == Material.FENCE
         || material == Material.FENCE_GATE
         || material == Material.ACACIA_FENCE
         || material == Material.BIRCH_FENCE
         || material == Material.DARK_OAK_FENCE
         || material == Material.JUNGLE_FENCE
         || material == Material.NETHER_FENCE
         || material == Material.SPRUCE_FENCE
         || material == Material.ACACIA_FENCE_GATE
         || material == Material.BIRCH_FENCE_GATE
         || material == Material.JUNGLE_FENCE_GATE
         || material == Material.DARK_OAK_FENCE_GATE
         || material == Material.SPRUCE_FENCE_GATE
         || material == Material.COBBLE_WALL;
   }

   public static boolean isContainerBlock(Block block) {
      return block.getType() == Material.CHEST || block.getType() == Material.ENDER_CHEST || block.getType() == Material.TRAPPED_CHEST;
   }

   public static boolean isWalkableSurface(Block block) {
      return (!isFenceOrGate(block.getType()) || block.getRelative(BlockFace.UP).getType() == Material.CARPET) && !isContainerBlock(block) && !MovementObstacleChecks.isCarpetOrPressurePlate(block);
   }

   public static boolean isOnGround(Location location) {
      return isOnGround(location, new RelativeBlockCache(location));
   }

   public static boolean isOnGround(Location location, RelativeBlockCache relativeBlockCache) {
      if (location.getBlock().isLiquid()
         || location.clone().add(0.0, 1.0, 0.0).getBlock().isLiquid()
         || location.clone().add(0.0, 1.7, 0.0).getBlock().isLiquid()
         || location.clone().add(0.0, 2.5, 0.0).getBlock().isLiquid()) {
         return true;
      }

      if (location.clone().getBlock().getType() == Material.WEB && location.getY() % 1.0 != 0.0) {
         return false;
      }

      if (isLava(relativeBlockCache.getSubtractedBlock(0.0, 0.1, 0.3))
         || isLava(relativeBlockCache.getSubtractedBlock(0.3, 0.1, 0.0))
         || isLava(relativeBlockCache.getSubtractedBlock(0.0, 0.1, -0.3))
         || isLava(relativeBlockCache.getSubtractedBlock(-0.3, 0.1, 0.0))
         || isLava(relativeBlockCache.getSubtractedBlock(-0.3, 0.1, -0.3))
         || isLava(relativeBlockCache.getSubtractedBlock(-0.3, 0.1, 0.3))
         || isLava(relativeBlockCache.getSubtractedBlock(0.3, 0.1, -0.3))
         || isLava(relativeBlockCache.getSubtractedBlock(0.3, 0.1, 0.3))) {
         return true;
      }

      if (isStepable(location.clone().subtract(0.0, 0.2, 0.0).getBlock()) || isStepable(location.getBlock())) {
         return true;
      }

      if (!isContainerBlock(relativeBlockCache.getSubtractedBlock(0.0, 0.1, 0.235))
         && !isContainerBlock(relativeBlockCache.getSubtractedBlock(0.235, 0.1, 0.0))
         && !isContainerBlock(relativeBlockCache.getSubtractedBlock(0.0, 0.1, -0.235))
         && !isContainerBlock(relativeBlockCache.getSubtractedBlock(-0.235, 0.1, 0.0))
         && !isContainerBlock(relativeBlockCache.getSubtractedBlock(-0.235, 0.1, -0.235))
         && !isContainerBlock(relativeBlockCache.getSubtractedBlock(-0.235, 0.1, 0.235))
         && !isContainerBlock(relativeBlockCache.getSubtractedBlock(0.235, 0.1, -0.235))
         && !isContainerBlock(relativeBlockCache.getSubtractedBlock(0.235, 0.1, 0.235))) {
         boolean flag = MovementObstacleChecks.isCarpetOrPressurePlate(relativeBlockCache.getSubtractedBlock(0.0, 0.6, 0.1))
            || MovementObstacleChecks.isCarpetOrPressurePlate(relativeBlockCache.getSubtractedBlock(0.1, 0.6, 0.0))
            || MovementObstacleChecks.isCarpetOrPressurePlate(relativeBlockCache.getSubtractedBlock(0.0, 0.6, -0.1))
            || MovementObstacleChecks.isCarpetOrPressurePlate(relativeBlockCache.getSubtractedBlock(-0.1, 0.6, 0.0))
            || MovementObstacleChecks.isCarpetOrPressurePlate(relativeBlockCache.getSubtractedBlock(-0.1, 0.6, -0.1))
            || MovementObstacleChecks.isCarpetOrPressurePlate(relativeBlockCache.getSubtractedBlock(-0.1, 0.6, 0.1))
            || MovementObstacleChecks.isCarpetOrPressurePlate(relativeBlockCache.getSubtractedBlock(0.1, 0.6, -0.1))
            || MovementObstacleChecks.isCarpetOrPressurePlate(relativeBlockCache.getSubtractedBlock(0.1, 0.6, 0.1));
         if ((!MovementObstacleChecks.isCarpetOrPressurePlate(relativeBlockCache.getSubtractedBlock(0.0, 0.1, 0.3)) || MovementObstacleChecks.isCarpetOrPressurePlate(relativeBlockCache.getSubtractedBlock(0.0, 0.0, 0.3)) && !flag)
            && (!MovementObstacleChecks.isCarpetOrPressurePlate(relativeBlockCache.getSubtractedBlock(0.3, 0.1, 0.0)) || MovementObstacleChecks.isCarpetOrPressurePlate(relativeBlockCache.getSubtractedBlock(0.3, 0.0, 0.0)) && !flag)
            && (!MovementObstacleChecks.isCarpetOrPressurePlate(relativeBlockCache.getSubtractedBlock(0.0, 0.1, -0.3)) || MovementObstacleChecks.isCarpetOrPressurePlate(relativeBlockCache.getSubtractedBlock(0.0, 0.0, -0.3)) && !flag)
            && (!MovementObstacleChecks.isCarpetOrPressurePlate(relativeBlockCache.getSubtractedBlock(-0.3, 0.1, 0.0)) || MovementObstacleChecks.isCarpetOrPressurePlate(relativeBlockCache.getSubtractedBlock(-0.3, 0.0, 0.0)) && !flag)
            && (!MovementObstacleChecks.isCarpetOrPressurePlate(relativeBlockCache.getSubtractedBlock(-0.3, 0.1, -0.3)) || MovementObstacleChecks.isCarpetOrPressurePlate(relativeBlockCache.getSubtractedBlock(-0.3, 0.0, -0.3)) && !flag)
            && (!MovementObstacleChecks.isCarpetOrPressurePlate(relativeBlockCache.getSubtractedBlock(-0.3, 0.1, 0.3)) || MovementObstacleChecks.isCarpetOrPressurePlate(relativeBlockCache.getSubtractedBlock(-0.3, 0.0, 0.3)) && !flag)
            && (!MovementObstacleChecks.isCarpetOrPressurePlate(relativeBlockCache.getSubtractedBlock(0.3, 0.1, -0.3)) || MovementObstacleChecks.isCarpetOrPressurePlate(relativeBlockCache.getSubtractedBlock(0.3, 0.0, -0.3)) && !flag)
            && (!MovementObstacleChecks.isCarpetOrPressurePlate(relativeBlockCache.getSubtractedBlock(0.3, 0.1, 0.0)) || MovementObstacleChecks.isCarpetOrPressurePlate(relativeBlockCache.getSubtractedBlock(0.3, 0.0, 0.3)) && !flag)) {
            boolean flag2 = isLava(relativeBlockCache.getSubtractedBlock(0.0, 0.6, 0.1))
               || isLava(relativeBlockCache.getSubtractedBlock(0.1, 0.6, 0.0))
               || isLava(relativeBlockCache.getSubtractedBlock(0.0, 0.6, -0.1))
               || isLava(relativeBlockCache.getSubtractedBlock(-0.1, 0.6, 0.0))
               || isLava(relativeBlockCache.getSubtractedBlock(-0.1, 0.6, -0.1))
               || isLava(relativeBlockCache.getSubtractedBlock(-0.1, 0.6, 0.1))
               || isLava(relativeBlockCache.getSubtractedBlock(0.1, 0.6, -0.1))
               || isLava(relativeBlockCache.getSubtractedBlock(0.1, 0.6, 0.1));
            if ((!isFenceOrGate(relativeBlockCache.getSubtractedMaterial(0.0, 0.6, 0.3)) || isFenceOrGate(relativeBlockCache.getSubtractedMaterial(0.0, 0.3, 0.3)) && !flag2)
               && (!isFenceOrGate(relativeBlockCache.getSubtractedMaterial(0.3, 0.6, 0.0)) || isFenceOrGate(relativeBlockCache.getSubtractedMaterial(0.3, 0.3, 0.0)) && !flag2)
               && (!isFenceOrGate(relativeBlockCache.getSubtractedMaterial(0.0, 0.6, -0.3)) || isFenceOrGate(relativeBlockCache.getSubtractedMaterial(0.0, 0.3, -0.3)) && !flag2)
               && (!isFenceOrGate(relativeBlockCache.getSubtractedMaterial(-0.3, 0.6, 0.0)) || isFenceOrGate(relativeBlockCache.getSubtractedMaterial(-0.3, 0.3, 0.0)) && !flag2)
               && (!isFenceOrGate(relativeBlockCache.getSubtractedMaterial(-0.3, 0.6, -0.3)) || isFenceOrGate(relativeBlockCache.getSubtractedMaterial(-0.3, 0.3, -0.3)) && !flag2)
               && (!isFenceOrGate(relativeBlockCache.getSubtractedMaterial(-0.3, 0.6, 0.3)) || isFenceOrGate(relativeBlockCache.getSubtractedMaterial(-0.3, 0.3, 0.3)) && !flag2)
               && (!isFenceOrGate(relativeBlockCache.getSubtractedMaterial(0.3, 0.6, -0.3)) || isFenceOrGate(relativeBlockCache.getSubtractedMaterial(0.3, 0.3, -0.3)) && !flag2)
               && (!isFenceOrGate(relativeBlockCache.getSubtractedMaterial(0.3, 0.6, 0.0)) || isFenceOrGate(relativeBlockCache.getSubtractedMaterial(0.3, 0.3, 0.3)) && !flag2)) {
               Block block = getSupportingSpecialBlock(relativeBlockCache);
               double y = location.getY();
               int value = (int)y;
               y -= value;
               if (block != null) {
                  if (block.getType() == Material.SNOW) {
                     byte b = block.getData();
                     if (b > 0 && y - 0.1 < b * 0.125) {
                        return true;
                     }
                  } else if (isPartialHeightBlock(block.getType()) && y - 0.1 < 0.125) {
                     return true;
                  }
               }

               return location.clone().subtract(0.0, 0.6, 0.0).getBlock().getType() == Material.COBBLE_WALL
                  && isFenceOrGate(location.getBlock().getType())
                  && location.getBlock().getType() != Material.COBBLE_WALL;
            } else {
               return true;
            }
         } else {
            return true;
         }
      } else {
         return true;
      }
   }

   public static boolean isNearLava(RelativeBlockCache relativeBlockCache) {
      return isLava(relativeBlockCache.getSubtractedBlock(0.0, 0.1, 0.5))
         || isLava(relativeBlockCache.getSubtractedBlock(0.5, 0.1, 0.0))
         || isLava(relativeBlockCache.getSubtractedBlock(0.0, 0.1, -0.5))
         || isLava(relativeBlockCache.getSubtractedBlock(-0.5, 0.1, 0.0))
         || isLava(relativeBlockCache.getSubtractedBlock(-0.5, 0.1, -0.5))
         || isLava(relativeBlockCache.getSubtractedBlock(-0.5, 0.1, 0.5))
         || isLava(relativeBlockCache.getSubtractedBlock(0.5, 0.1, -0.5))
         || isLava(relativeBlockCache.getSubtractedBlock(0.5, 0.1, 0.5));
   }

   public static boolean isUnderLowCeiling(RelativeBlockCache relativeBlockCache) {
      return isFullOrHalfBlock(relativeBlockCache.getSubtractedBlock(0.0, 0.1, 0.3))
         || isFullOrHalfBlock(relativeBlockCache.getSubtractedBlock(0.3, 0.1, 0.0))
         || isFullOrHalfBlock(relativeBlockCache.getSubtractedBlock(0.0, 0.1, -0.3))
         || isFullOrHalfBlock(relativeBlockCache.getSubtractedBlock(-0.3, 0.1, 0.0))
         || isFullOrHalfBlock(relativeBlockCache.getSubtractedBlock(-0.3, 0.1, -0.3))
         || isFullOrHalfBlock(relativeBlockCache.getSubtractedBlock(-0.3, 0.1, 0.3))
         || isFullOrHalfBlock(relativeBlockCache.getSubtractedBlock(0.3, 0.1, -0.3))
         || isFullOrHalfBlock(relativeBlockCache.getSubtractedBlock(0.3, 0.1, 0.3));
   }

   private static Block getSupportingSpecialBlock(RelativeBlockCache relativeBlockCache) {
      HashSet<Block> set = new HashSet<>();
      set.add(relativeBlockCache.getRelativeBlock(0.0, -0.1, 0.3));
      set.add(relativeBlockCache.getRelativeBlock(0.3, -0.1, 0.0));
      set.add(relativeBlockCache.getRelativeBlock(0.0, -0.1, -0.3));
      set.add(relativeBlockCache.getRelativeBlock(-0.3, -0.1, 0.0));
      set.add(relativeBlockCache.getRelativeBlock(-0.3, -0.1, -0.3));
      set.add(relativeBlockCache.getRelativeBlock(-0.3, -0.1, 0.3));
      set.add(relativeBlockCache.getRelativeBlock(0.3, -0.1, -0.3));
      set.add(relativeBlockCache.getRelativeBlock(0.3, -0.1, 0.3));
      set.add(relativeBlockCache.getRelativeBlock(0.0, -0.1, 0.0));
      Block block = null;
      byte b = -1;

      for (Block block2 : set) {
         if (block2.getType() == Material.SNOW) {
            byte b2 = block2.getData();
            if (b2 > b) {
               b = b2;
               block = block2;
            }
         }
      }

      for (Block block3 : set) {
         if (isPartialHeightBlock(block3.getType()) && 1 > b) {
            b = 1;
            block = block3;
         }
      }

      return block;
   }

   public static boolean isOnClimbable(Player player) {
      return player.getLocation().getBlock().getType().equals(Material.LADDER) || player.getLocation().getBlock().getType().equals(Material.VINE);
   }

   public static boolean isInLiquid(Player player) {
      return player.getLocation().getBlock().isLiquid() || player.getLocation().getBlock().getRelative(BlockFace.UP).isLiquid();
   }

   public static boolean isPartialHeightBlock(Material material) {
      switch (MaterialSwitch.SWITCH_TABLE[material.ordinal()]) {
         case 149:
         case 150:
         case 151:
         case 152:
            return true;
         default:
            return false;
      }
   }

   public static boolean isAdjacentToSolidBlock(RelativeBlockCache relativeBlockCache) {
      Block block = relativeBlockCache.getCenterBlock();

      for (BlockFace face : BlockFace.values()) {
         Material material = block.getRelative(face).getType();
         switch (MaterialSwitch.SWITCH_TABLE[material.ordinal()]) {
            case 96:
            case 104:
            case 116:
            case 118:
            case 121:
            case 138:
            case 147:
            case 148:
            case 153:
               return true;
         }
      }

      return false;
   }

   public static boolean isAboveBounceBlock(RelativeBlockCache relativeBlockCache) {
      return isSlimeOrMovingPiston(relativeBlockCache.getSubtractedBlock(0.0, -1.0, 0.4))
         || isSlimeOrMovingPiston(relativeBlockCache.getSubtractedBlock(0.4, -1.0, 0.0))
         || isSlimeOrMovingPiston(relativeBlockCache.getSubtractedBlock(0.0, -1.0, -0.4))
         || isSlimeOrMovingPiston(relativeBlockCache.getSubtractedBlock(-0.4, -1.0, 0.0))
         || isSlimeOrMovingPiston(relativeBlockCache.getSubtractedBlock(-0.4, -1.0, -0.4))
         || isSlimeOrMovingPiston(relativeBlockCache.getSubtractedBlock(-0.4, -1.0, 0.4))
         || isSlimeOrMovingPiston(relativeBlockCache.getSubtractedBlock(0.4, -1.0, -0.4))
         || isSlimeOrMovingPiston(relativeBlockCache.getSubtractedBlock(0.4, -1.0, 0.4))
         || isSlimeOrMovingPiston(relativeBlockCache.getSubtractedBlock(0.0, 0.0, 0.4))
         || isSlimeOrMovingPiston(relativeBlockCache.getSubtractedBlock(0.4, 0.0, 0.0))
         || isSlimeOrMovingPiston(relativeBlockCache.getSubtractedBlock(0.0, 0.0, -0.4))
         || isSlimeOrMovingPiston(relativeBlockCache.getSubtractedBlock(-0.4, 0.0, 0.0))
         || isSlimeOrMovingPiston(relativeBlockCache.getSubtractedBlock(-0.4, 0.0, -0.4))
         || isSlimeOrMovingPiston(relativeBlockCache.getSubtractedBlock(-0.4, 0.0, 0.4))
         || isSlimeOrMovingPiston(relativeBlockCache.getSubtractedBlock(0.4, 0.0, -0.4))
         || isSlimeOrMovingPiston(relativeBlockCache.getSubtractedBlock(0.4, 0.0, 0.4))
         || isSlimeOrMovingPiston(relativeBlockCache.getSubtractedBlock(0.0, 1.0, 0.4))
         || isSlimeOrMovingPiston(relativeBlockCache.getSubtractedBlock(0.4, 1.0, 0.0))
         || isSlimeOrMovingPiston(relativeBlockCache.getSubtractedBlock(0.0, 1.0, -0.4))
         || isSlimeOrMovingPiston(relativeBlockCache.getSubtractedBlock(-0.4, 1.0, 0.0))
         || isSlimeOrMovingPiston(relativeBlockCache.getSubtractedBlock(-0.4, 1.0, -0.4))
         || isSlimeOrMovingPiston(relativeBlockCache.getSubtractedBlock(-0.4, 1.0, 0.4))
         || isSlimeOrMovingPiston(relativeBlockCache.getSubtractedBlock(0.4, 1.0, -0.4))
         || isSlimeOrMovingPiston(relativeBlockCache.getSubtractedBlock(0.4, 1.0, 0.4))
         || isSlimeOrMovingPiston(relativeBlockCache.getSubtractedBlock(0.0, 2.0, 0.4))
         || isSlimeOrMovingPiston(relativeBlockCache.getSubtractedBlock(0.4, 2.0, 0.0))
         || isSlimeOrMovingPiston(relativeBlockCache.getSubtractedBlock(0.0, 2.0, -0.4))
         || isSlimeOrMovingPiston(relativeBlockCache.getSubtractedBlock(-0.4, 2.0, 0.0))
         || isSlimeOrMovingPiston(relativeBlockCache.getSubtractedBlock(-0.4, 2.0, -0.4))
         || isSlimeOrMovingPiston(relativeBlockCache.getSubtractedBlock(-0.4, 2.0, 0.4))
         || isSlimeOrMovingPiston(relativeBlockCache.getSubtractedBlock(0.4, 2.0, -0.4))
         || isSlimeOrMovingPiston(relativeBlockCache.getSubtractedBlock(0.4, 2.0, 0.4))
         || isSlimeOrMovingPiston(relativeBlockCache.getSubtractedBlock(0.0, 3.0, 0.4))
         || isSlimeOrMovingPiston(relativeBlockCache.getSubtractedBlock(0.4, 3.0, 0.0))
         || isSlimeOrMovingPiston(relativeBlockCache.getSubtractedBlock(0.0, 3.0, -0.4))
         || isSlimeOrMovingPiston(relativeBlockCache.getSubtractedBlock(-0.4, 3.0, 0.0))
         || isSlimeOrMovingPiston(relativeBlockCache.getSubtractedBlock(-0.4, 3.0, -0.4))
         || isSlimeOrMovingPiston(relativeBlockCache.getSubtractedBlock(-0.4, 3.0, 0.4))
         || isSlimeOrMovingPiston(relativeBlockCache.getSubtractedBlock(0.4, 3.0, -0.4))
         || isSlimeOrMovingPiston(relativeBlockCache.getSubtractedBlock(0.4, 3.0, 0.4))
         || isSlimeOrMovingPiston(relativeBlockCache.getSubtractedBlock(0.0, 4.0, 0.4))
         || isSlimeOrMovingPiston(relativeBlockCache.getSubtractedBlock(0.4, 4.0, 0.0))
         || isSlimeOrMovingPiston(relativeBlockCache.getSubtractedBlock(0.0, 4.0, -0.4))
         || isSlimeOrMovingPiston(relativeBlockCache.getSubtractedBlock(-0.4, 4.0, 0.0))
         || isSlimeOrMovingPiston(relativeBlockCache.getSubtractedBlock(-0.4, 4.0, -0.4))
         || isSlimeOrMovingPiston(relativeBlockCache.getSubtractedBlock(-0.4, 4.0, 0.4))
         || isSlimeOrMovingPiston(relativeBlockCache.getSubtractedBlock(0.4, 4.0, -0.4))
         || isSlimeOrMovingPiston(relativeBlockCache.getSubtractedBlock(0.4, 4.0, 0.4))
         || relativeBlockCache.getSubtractedBlock(0.0, -1.0, 0.0).getType().equals(Material.SLIME_BLOCK)
         || relativeBlockCache.getCenterBlock().getType().equals(Material.SLIME_BLOCK)
         || relativeBlockCache.getSubtractedBlock(0.0, 1.0, 0.0).getType().equals(Material.SLIME_BLOCK)
         || relativeBlockCache.getSubtractedBlock(0.0, 2.0, 0.0).getType().equals(Material.SLIME_BLOCK);
   }

   public static boolean isSlimeOrMovingPiston(Block block) {
      return block.getType() == Material.SLIME_BLOCK || block.getType() == Material.PISTON_MOVING_PIECE;
   }

   class MaterialSwitch {
      static final int[] SWITCH_TABLE = new int[Material.values().length];

      static {
         try {
            SWITCH_TABLE[Material.BARRIER.ordinal()] = 1;
         } catch (NoSuchFieldError ex) {
         }

         try {
            SWITCH_TABLE[Material.BEACON.ordinal()] = 2;
         } catch (NoSuchFieldError ex2) {
         }

         try {
            SWITCH_TABLE[Material.BEDROCK.ordinal()] = 3;
         } catch (NoSuchFieldError ex3) {
         }

         try {
            SWITCH_TABLE[Material.BOOKSHELF.ordinal()] = 4;
         } catch (NoSuchFieldError ex4) {
         }

         try {
            SWITCH_TABLE[Material.BRICK.ordinal()] = 5;
         } catch (NoSuchFieldError ex5) {
         }

         try {
            SWITCH_TABLE[Material.BURNING_FURNACE.ordinal()] = 6;
         } catch (NoSuchFieldError ex6) {
         }

         try {
            SWITCH_TABLE[Material.CLAY.ordinal()] = 7;
         } catch (NoSuchFieldError ex7) {
         }

         try {
            SWITCH_TABLE[Material.CLAY_BRICK.ordinal()] = 8;
         } catch (NoSuchFieldError ex8) {
         }

         try {
            SWITCH_TABLE[Material.COAL_BLOCK.ordinal()] = 9;
         } catch (NoSuchFieldError ex9) {
         }

         try {
            SWITCH_TABLE[Material.COAL_ORE.ordinal()] = 10;
         } catch (NoSuchFieldError ex10) {
         }

         try {
            SWITCH_TABLE[Material.COBBLESTONE.ordinal()] = 11;
         } catch (NoSuchFieldError ex11) {
         }

         try {
            SWITCH_TABLE[Material.COMMAND.ordinal()] = 12;
         } catch (NoSuchFieldError ex12) {
         }

         try {
            SWITCH_TABLE[Material.DIAMOND_BLOCK.ordinal()] = 13;
         } catch (NoSuchFieldError ex13) {
         }

         try {
            SWITCH_TABLE[Material.DIAMOND_ORE.ordinal()] = 14;
         } catch (NoSuchFieldError ex14) {
         }

         try {
            SWITCH_TABLE[Material.DIRT.ordinal()] = 15;
         } catch (NoSuchFieldError ex15) {
         }

         try {
            SWITCH_TABLE[Material.DISPENSER.ordinal()] = 16;
         } catch (NoSuchFieldError ex16) {
         }

         try {
            SWITCH_TABLE[Material.DOUBLE_STEP.ordinal()] = 17;
         } catch (NoSuchFieldError ex17) {
         }

         try {
            SWITCH_TABLE[Material.DOUBLE_STONE_SLAB2.ordinal()] = 18;
         } catch (NoSuchFieldError ex18) {
         }

         try {
            SWITCH_TABLE[Material.DROPPER.ordinal()] = 19;
         } catch (NoSuchFieldError ex19) {
         }

         try {
            SWITCH_TABLE[Material.EMERALD_BLOCK.ordinal()] = 20;
         } catch (NoSuchFieldError ex20) {
         }

         try {
            SWITCH_TABLE[Material.EMERALD_ORE.ordinal()] = 21;
         } catch (NoSuchFieldError ex21) {
         }

         try {
            SWITCH_TABLE[Material.FURNACE.ordinal()] = 22;
         } catch (NoSuchFieldError ex22) {
         }

         try {
            SWITCH_TABLE[Material.GLASS.ordinal()] = 23;
         } catch (NoSuchFieldError ex23) {
         }

         try {
            SWITCH_TABLE[Material.GLOWING_REDSTONE_ORE.ordinal()] = 24;
         } catch (NoSuchFieldError ex24) {
         }

         try {
            SWITCH_TABLE[Material.GLOWSTONE.ordinal()] = 25;
         } catch (NoSuchFieldError ex25) {
         }

         try {
            SWITCH_TABLE[Material.GOLD_BLOCK.ordinal()] = 26;
         } catch (NoSuchFieldError ex26) {
         }

         try {
            SWITCH_TABLE[Material.GOLD_ORE.ordinal()] = 27;
         } catch (NoSuchFieldError ex27) {
         }

         try {
            SWITCH_TABLE[Material.GRASS.ordinal()] = 28;
         } catch (NoSuchFieldError ex28) {
         }

         try {
            SWITCH_TABLE[Material.GRAVEL.ordinal()] = 29;
         } catch (NoSuchFieldError ex29) {
         }

         try {
            SWITCH_TABLE[Material.HARD_CLAY.ordinal()] = 30;
         } catch (NoSuchFieldError ex30) {
         }

         try {
            SWITCH_TABLE[Material.HAY_BLOCK.ordinal()] = 31;
         } catch (NoSuchFieldError ex31) {
         }

         try {
            SWITCH_TABLE[Material.HUGE_MUSHROOM_1.ordinal()] = 32;
         } catch (NoSuchFieldError ex32) {
         }

         try {
            SWITCH_TABLE[Material.HUGE_MUSHROOM_2.ordinal()] = 33;
         } catch (NoSuchFieldError ex33) {
         }

         try {
            SWITCH_TABLE[Material.ICE.ordinal()] = 34;
         } catch (NoSuchFieldError ex34) {
         }

         try {
            SWITCH_TABLE[Material.IRON_BLOCK.ordinal()] = 35;
         } catch (NoSuchFieldError ex35) {
         }

         try {
            SWITCH_TABLE[Material.IRON_ORE.ordinal()] = 36;
         } catch (NoSuchFieldError ex36) {
         }

         try {
            SWITCH_TABLE[Material.JACK_O_LANTERN.ordinal()] = 37;
         } catch (NoSuchFieldError ex37) {
         }

         try {
            SWITCH_TABLE[Material.JUKEBOX.ordinal()] = 38;
         } catch (NoSuchFieldError ex38) {
         }

         try {
            SWITCH_TABLE[Material.LAPIS_BLOCK.ordinal()] = 39;
         } catch (NoSuchFieldError ex39) {
         }

         try {
            SWITCH_TABLE[Material.LAPIS_ORE.ordinal()] = 40;
         } catch (NoSuchFieldError ex40) {
         }

         try {
            SWITCH_TABLE[Material.LEAVES.ordinal()] = 41;
         } catch (NoSuchFieldError ex41) {
         }

         try {
            SWITCH_TABLE[Material.LEAVES_2.ordinal()] = 42;
         } catch (NoSuchFieldError ex42) {
         }

         try {
            SWITCH_TABLE[Material.LOG.ordinal()] = 43;
         } catch (NoSuchFieldError ex43) {
         }

         try {
            SWITCH_TABLE[Material.LOG_2.ordinal()] = 44;
         } catch (NoSuchFieldError ex44) {
         }

         try {
            SWITCH_TABLE[Material.MOB_SPAWNER.ordinal()] = 45;
         } catch (NoSuchFieldError ex45) {
         }

         try {
            SWITCH_TABLE[Material.MOSSY_COBBLESTONE.ordinal()] = 46;
         } catch (NoSuchFieldError ex46) {
         }

         try {
            SWITCH_TABLE[Material.MYCEL.ordinal()] = 47;
         } catch (NoSuchFieldError ex47) {
         }

         try {
            SWITCH_TABLE[Material.NETHERRACK.ordinal()] = 48;
         } catch (NoSuchFieldError ex48) {
         }

         try {
            SWITCH_TABLE[Material.NETHER_BRICK.ordinal()] = 49;
         } catch (NoSuchFieldError ex49) {
         }

         try {
            SWITCH_TABLE[Material.NETHER_BRICK_ITEM.ordinal()] = 50;
         } catch (NoSuchFieldError ex50) {
         }

         try {
            SWITCH_TABLE[Material.NOTE_BLOCK.ordinal()] = 51;
         } catch (NoSuchFieldError ex51) {
         }

         try {
            SWITCH_TABLE[Material.OBSIDIAN.ordinal()] = 52;
         } catch (NoSuchFieldError ex52) {
         }

         try {
            SWITCH_TABLE[Material.PACKED_ICE.ordinal()] = 53;
         } catch (NoSuchFieldError ex53) {
         }

         try {
            SWITCH_TABLE[Material.PISTON_BASE.ordinal()] = 54;
         } catch (NoSuchFieldError ex54) {
         }

         try {
            SWITCH_TABLE[Material.PISTON_STICKY_BASE.ordinal()] = 55;
         } catch (NoSuchFieldError ex55) {
         }

         try {
            SWITCH_TABLE[Material.PRISMARINE.ordinal()] = 56;
         } catch (NoSuchFieldError ex56) {
         }

         try {
            SWITCH_TABLE[Material.PUMPKIN.ordinal()] = 57;
         } catch (NoSuchFieldError ex57) {
         }

         try {
            SWITCH_TABLE[Material.QUARTZ_BLOCK.ordinal()] = 58;
         } catch (NoSuchFieldError ex58) {
         }

         try {
            SWITCH_TABLE[Material.QUARTZ_ORE.ordinal()] = 59;
         } catch (NoSuchFieldError ex59) {
         }

         try {
            SWITCH_TABLE[Material.REDSTONE_BLOCK.ordinal()] = 60;
         } catch (NoSuchFieldError ex60) {
         }

         try {
            SWITCH_TABLE[Material.REDSTONE_LAMP_OFF.ordinal()] = 61;
         } catch (NoSuchFieldError ex61) {
         }

         try {
            SWITCH_TABLE[Material.REDSTONE_LAMP_ON.ordinal()] = 62;
         } catch (NoSuchFieldError ex62) {
         }

         try {
            SWITCH_TABLE[Material.REDSTONE_ORE.ordinal()] = 63;
         } catch (NoSuchFieldError ex63) {
         }

         try {
            SWITCH_TABLE[Material.RED_SANDSTONE.ordinal()] = 64;
         } catch (NoSuchFieldError ex64) {
         }

         try {
            SWITCH_TABLE[Material.SAND.ordinal()] = 65;
         } catch (NoSuchFieldError ex65) {
         }

         try {
            SWITCH_TABLE[Material.SANDSTONE.ordinal()] = 66;
         } catch (NoSuchFieldError ex66) {
         }

         try {
            SWITCH_TABLE[Material.SEA_LANTERN.ordinal()] = 67;
         } catch (NoSuchFieldError ex67) {
         }

         try {
            SWITCH_TABLE[Material.SMOOTH_BRICK.ordinal()] = 68;
         } catch (NoSuchFieldError ex68) {
         }

         try {
            SWITCH_TABLE[Material.SNOW_BLOCK.ordinal()] = 69;
         } catch (NoSuchFieldError ex69) {
         }

         try {
            SWITCH_TABLE[Material.SPONGE.ordinal()] = 70;
         } catch (NoSuchFieldError ex70) {
         }

         try {
            SWITCH_TABLE[Material.STAINED_CLAY.ordinal()] = 71;
         } catch (NoSuchFieldError ex71) {
         }

         try {
            SWITCH_TABLE[Material.STAINED_GLASS.ordinal()] = 72;
         } catch (NoSuchFieldError ex72) {
         }

         try {
            SWITCH_TABLE[Material.STONE.ordinal()] = 73;
         } catch (NoSuchFieldError ex73) {
         }

         try {
            SWITCH_TABLE[Material.TNT.ordinal()] = 74;
         } catch (NoSuchFieldError ex74) {
         }

         try {
            SWITCH_TABLE[Material.WOOD.ordinal()] = 75;
         } catch (NoSuchFieldError ex75) {
         }

         try {
            SWITCH_TABLE[Material.WOOD_DOUBLE_STEP.ordinal()] = 76;
         } catch (NoSuchFieldError ex76) {
         }

         try {
            SWITCH_TABLE[Material.WOOL.ordinal()] = 77;
         } catch (NoSuchFieldError ex77) {
         }

         try {
            SWITCH_TABLE[Material.WORKBENCH.ordinal()] = 78;
         } catch (NoSuchFieldError ex78) {
         }

         try {
            SWITCH_TABLE[Material.BED_BLOCK.ordinal()] = 79;
         } catch (NoSuchFieldError ex79) {
         }

         try {
            SWITCH_TABLE[Material.PISTON_EXTENSION.ordinal()] = 80;
         } catch (NoSuchFieldError ex80) {
         }

         try {
            SWITCH_TABLE[Material.PISTON_MOVING_PIECE.ordinal()] = 81;
         } catch (NoSuchFieldError ex81) {
         }

         try {
            SWITCH_TABLE[Material.STEP.ordinal()] = 82;
         } catch (NoSuchFieldError ex82) {
         }

         try {
            SWITCH_TABLE[Material.WOOD_STAIRS.ordinal()] = 83;
         } catch (NoSuchFieldError ex83) {
         }

         try {
            SWITCH_TABLE[Material.CHEST.ordinal()] = 84;
         } catch (NoSuchFieldError ex84) {
         }

         try {
            SWITCH_TABLE[Material.SOIL.ordinal()] = 85;
         } catch (NoSuchFieldError ex85) {
         }

         try {
            SWITCH_TABLE[Material.WOODEN_DOOR.ordinal()] = 86;
         } catch (NoSuchFieldError ex86) {
         }

         try {
            SWITCH_TABLE[Material.COBBLESTONE_STAIRS.ordinal()] = 87;
         } catch (NoSuchFieldError ex87) {
         }

         try {
            SWITCH_TABLE[Material.IRON_DOOR_BLOCK.ordinal()] = 88;
         } catch (NoSuchFieldError ex88) {
         }

         try {
            SWITCH_TABLE[Material.CACTUS.ordinal()] = 89;
         } catch (NoSuchFieldError ex89) {
         }

         try {
            SWITCH_TABLE[Material.FENCE.ordinal()] = 90;
         } catch (NoSuchFieldError ex90) {
         }

         try {
            SWITCH_TABLE[Material.SOUL_SAND.ordinal()] = 91;
         } catch (NoSuchFieldError ex91) {
         }

         try {
            SWITCH_TABLE[Material.CAKE_BLOCK.ordinal()] = 92;
         } catch (NoSuchFieldError ex92) {
         }

         try {
            SWITCH_TABLE[Material.TRAP_DOOR.ordinal()] = 93;
         } catch (NoSuchFieldError ex93) {
         }

         try {
            SWITCH_TABLE[Material.MONSTER_EGGS.ordinal()] = 94;
         } catch (NoSuchFieldError ex94) {
         }

         try {
            SWITCH_TABLE[Material.IRON_FENCE.ordinal()] = 95;
         } catch (NoSuchFieldError ex95) {
         }

         try {
            SWITCH_TABLE[Material.THIN_GLASS.ordinal()] = 96;
         } catch (NoSuchFieldError ex96) {
         }

         try {
            SWITCH_TABLE[Material.MELON_BLOCK.ordinal()] = 97;
         } catch (NoSuchFieldError ex97) {
         }

         try {
            SWITCH_TABLE[Material.FENCE_GATE.ordinal()] = 98;
         } catch (NoSuchFieldError ex98) {
         }

         try {
            SWITCH_TABLE[Material.BRICK_STAIRS.ordinal()] = 99;
         } catch (NoSuchFieldError ex99) {
         }

         try {
            SWITCH_TABLE[Material.SMOOTH_STAIRS.ordinal()] = 100;
         } catch (NoSuchFieldError ex100) {
         }

         try {
            SWITCH_TABLE[Material.NETHER_FENCE.ordinal()] = 101;
         } catch (NoSuchFieldError ex101) {
         }

         try {
            SWITCH_TABLE[Material.NETHER_BRICK_STAIRS.ordinal()] = 102;
         } catch (NoSuchFieldError ex102) {
         }

         try {
            SWITCH_TABLE[Material.ENCHANTMENT_TABLE.ordinal()] = 103;
         } catch (NoSuchFieldError ex103) {
         }

         try {
            SWITCH_TABLE[Material.BREWING_STAND.ordinal()] = 104;
         } catch (NoSuchFieldError ex104) {
         }

         try {
            SWITCH_TABLE[Material.CAULDRON.ordinal()] = 105;
         } catch (NoSuchFieldError ex105) {
         }

         try {
            SWITCH_TABLE[Material.ENDER_PORTAL_FRAME.ordinal()] = 106;
         } catch (NoSuchFieldError ex106) {
         }

         try {
            SWITCH_TABLE[Material.ENDER_STONE.ordinal()] = 107;
         } catch (NoSuchFieldError ex107) {
         }

         try {
            SWITCH_TABLE[Material.DRAGON_EGG.ordinal()] = 108;
         } catch (NoSuchFieldError ex108) {
         }

         try {
            SWITCH_TABLE[Material.WOOD_STEP.ordinal()] = 109;
         } catch (NoSuchFieldError ex109) {
         }

         try {
            SWITCH_TABLE[Material.SANDSTONE_STAIRS.ordinal()] = 110;
         } catch (NoSuchFieldError ex110) {
         }

         try {
            SWITCH_TABLE[Material.ENDER_CHEST.ordinal()] = 111;
         } catch (NoSuchFieldError ex111) {
         }

         try {
            SWITCH_TABLE[Material.SPRUCE_WOOD_STAIRS.ordinal()] = 112;
         } catch (NoSuchFieldError ex112) {
         }

         try {
            SWITCH_TABLE[Material.BIRCH_WOOD_STAIRS.ordinal()] = 113;
         } catch (NoSuchFieldError ex113) {
         }

         try {
            SWITCH_TABLE[Material.JUNGLE_WOOD_STAIRS.ordinal()] = 114;
         } catch (NoSuchFieldError ex114) {
         }

         try {
            SWITCH_TABLE[Material.COBBLE_WALL.ordinal()] = 115;
         } catch (NoSuchFieldError ex115) {
         }

         try {
            SWITCH_TABLE[Material.ANVIL.ordinal()] = 116;
         } catch (NoSuchFieldError ex116) {
         }

         try {
            SWITCH_TABLE[Material.TRAPPED_CHEST.ordinal()] = 117;
         } catch (NoSuchFieldError ex117) {
         }

         try {
            SWITCH_TABLE[Material.DAYLIGHT_DETECTOR.ordinal()] = 118;
         } catch (NoSuchFieldError ex118) {
         }

         try {
            SWITCH_TABLE[Material.HOPPER.ordinal()] = 119;
         } catch (NoSuchFieldError ex119) {
         }

         try {
            SWITCH_TABLE[Material.QUARTZ_STAIRS.ordinal()] = 120;
         } catch (NoSuchFieldError ex120) {
         }

         try {
            SWITCH_TABLE[Material.STAINED_GLASS_PANE.ordinal()] = 121;
         } catch (NoSuchFieldError ex121) {
         }

         try {
            SWITCH_TABLE[Material.ACACIA_STAIRS.ordinal()] = 122;
         } catch (NoSuchFieldError ex122) {
         }

         try {
            SWITCH_TABLE[Material.DARK_OAK_STAIRS.ordinal()] = 123;
         } catch (NoSuchFieldError ex123) {
         }

         try {
            SWITCH_TABLE[Material.SLIME_BLOCK.ordinal()] = 124;
         } catch (NoSuchFieldError ex124) {
         }

         try {
            SWITCH_TABLE[Material.IRON_TRAPDOOR.ordinal()] = 125;
         } catch (NoSuchFieldError ex125) {
         }

         try {
            SWITCH_TABLE[Material.RED_SANDSTONE_STAIRS.ordinal()] = 126;
         } catch (NoSuchFieldError ex126) {
         }

         try {
            SWITCH_TABLE[Material.STONE_SLAB2.ordinal()] = 127;
         } catch (NoSuchFieldError ex127) {
         }

         try {
            SWITCH_TABLE[Material.SPRUCE_FENCE_GATE.ordinal()] = 128;
         } catch (NoSuchFieldError ex128) {
         }

         try {
            SWITCH_TABLE[Material.BIRCH_FENCE_GATE.ordinal()] = 129;
         } catch (NoSuchFieldError ex129) {
         }

         try {
            SWITCH_TABLE[Material.JUNGLE_FENCE_GATE.ordinal()] = 130;
         } catch (NoSuchFieldError ex130) {
         }

         try {
            SWITCH_TABLE[Material.DARK_OAK_FENCE_GATE.ordinal()] = 131;
         } catch (NoSuchFieldError ex131) {
         }

         try {
            SWITCH_TABLE[Material.ACACIA_FENCE_GATE.ordinal()] = 132;
         } catch (NoSuchFieldError ex132) {
         }

         try {
            SWITCH_TABLE[Material.SPRUCE_FENCE.ordinal()] = 133;
         } catch (NoSuchFieldError ex133) {
         }

         try {
            SWITCH_TABLE[Material.BIRCH_FENCE.ordinal()] = 134;
         } catch (NoSuchFieldError ex134) {
         }

         try {
            SWITCH_TABLE[Material.JUNGLE_FENCE.ordinal()] = 135;
         } catch (NoSuchFieldError ex135) {
         }

         try {
            SWITCH_TABLE[Material.DARK_OAK_FENCE.ordinal()] = 136;
         } catch (NoSuchFieldError ex136) {
         }

         try {
            SWITCH_TABLE[Material.ACACIA_FENCE.ordinal()] = 137;
         } catch (NoSuchFieldError ex137) {
         }

         try {
            SWITCH_TABLE[Material.DAYLIGHT_DETECTOR_INVERTED.ordinal()] = 138;
         } catch (NoSuchFieldError ex138) {
         }

         try {
            SWITCH_TABLE[Material.SPRUCE_DOOR.ordinal()] = 139;
         } catch (NoSuchFieldError ex139) {
         }

         try {
            SWITCH_TABLE[Material.BIRCH_DOOR.ordinal()] = 140;
         } catch (NoSuchFieldError ex140) {
         }

         try {
            SWITCH_TABLE[Material.JUNGLE_DOOR.ordinal()] = 141;
         } catch (NoSuchFieldError ex141) {
         }

         try {
            SWITCH_TABLE[Material.ACACIA_DOOR.ordinal()] = 142;
         } catch (NoSuchFieldError ex142) {
         }

         try {
            SWITCH_TABLE[Material.DARK_OAK_DOOR.ordinal()] = 143;
         } catch (NoSuchFieldError ex143) {
         }

         try {
            SWITCH_TABLE[Material.LADDER.ordinal()] = 144;
         } catch (NoSuchFieldError ex144) {
         }

         try {
            SWITCH_TABLE[Material.VINE.ordinal()] = 145;
         } catch (NoSuchFieldError ex145) {
         }

         try {
            SWITCH_TABLE[Material.WATER_LILY.ordinal()] = 146;
         } catch (NoSuchFieldError ex146) {
         }

         try {
            SWITCH_TABLE[Material.SKULL.ordinal()] = 147;
         } catch (NoSuchFieldError ex147) {
         }

         try {
            SWITCH_TABLE[Material.FLOWER_POT.ordinal()] = 148;
         } catch (NoSuchFieldError ex148) {
         }

         try {
            SWITCH_TABLE[Material.DIODE_BLOCK_OFF.ordinal()] = 149;
         } catch (NoSuchFieldError ex149) {
         }

         try {
            SWITCH_TABLE[Material.DIODE_BLOCK_ON.ordinal()] = 150;
         } catch (NoSuchFieldError ex150) {
         }

         try {
            SWITCH_TABLE[Material.REDSTONE_COMPARATOR_OFF.ordinal()] = 151;
         } catch (NoSuchFieldError ex151) {
         }

         try {
            SWITCH_TABLE[Material.REDSTONE_COMPARATOR_ON.ordinal()] = 152;
         } catch (NoSuchFieldError ex152) {
         }

         try {
            SWITCH_TABLE[Material.SNOW.ordinal()] = 153;
         } catch (NoSuchFieldError ex153) {
         }
      }
   }
}
