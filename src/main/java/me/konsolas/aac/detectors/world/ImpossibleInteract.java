package me.konsolas.aac.detectors.world;

import me.konsolas.aac.AAC;
import me.konsolas.aac.api.HackType;
import me.konsolas.aac.detectors.Detector;
import me.konsolas.aac.util.blocks.BlockCollisionChecks;
import me.konsolas.aac.util.blocks.BlockUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.BlockIterator;

public class ImpossibleInteract extends Detector {
   private boolean cancelLiquids = true;
   private boolean cancelGhosthand = true;

   public ImpossibleInteract(AAC plugin) {
      super(plugin);
      this.cancelLiquids = this.plugin.getConfig().getBoolean("impossibleinteract.cancel_liquids", this.cancelLiquids);
      this.cancelGhosthand = this.plugin.getConfig().getBoolean("impossibleinteract.cancel_ghosthand", this.cancelGhosthand);
   }

   @EventHandler
   public void onPlayerInteract(PlayerInteractEvent event) {
      if (event.getClickedBlock() != null && event.getClickedBlock().getType() != Material.AIR) {
         if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (this.plugin.checkManager.isPlayerExempt(event.getPlayer())) {
               return;
            }

            if (this.cancelGhosthand) {
               if (BlockUtil.getBreakTicks(event.getClickedBlock().getType(), event.getPlayer()) < 2L) {
                  return;
               }

               BlockIterator blockIterator;
               try {
                  blockIterator = new BlockIterator(event.getPlayer(), 7);
               } catch (IllegalStateException ex) {
                  return;
               }

               boolean flag = true;
               boolean flag2 = false;

               while (blockIterator.hasNext()) {
                  Block block = blockIterator.next();
                  if (block.equals(event.getClickedBlock())) {
                     flag2 = true;
                  }

                  if (BlockCollisionChecks.isSolidGroundBlock(block) && !flag2) {
                     flag = false;
                  }
               }

               if (!flag && flag2) {
                  Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new VerifyLineOfSightTask(this, event));
               }
            }
         }

         this.decreaseViolationLevel(event.getPlayer().getUniqueId(), 1);
      }
   }

   @EventHandler
   public void onBlockBreak(BlockBreakEvent event) {
      if (event.getBlock() != null && event.getBlock().getType() != Material.AIR && !this.plugin.checkManager.isPlayerExempt(event.getPlayer())) {
         if (BlockUtil.getBreakTicks(event.getBlock().getType(), event.getPlayer()) > 2L) {
            if (this.cancelGhosthand) {
               BlockIterator blockIterator;
               try {
                  blockIterator = new BlockIterator(event.getPlayer(), 6);
               } catch (IllegalStateException ex) {
                  return;
               }

               boolean flag = false;

               while (blockIterator.hasNext()) {
                  Block block = blockIterator.next();
                  if (block.equals(event.getBlock())) {
                     flag = true;
                     break;
                  }

                  if (BlockCollisionChecks.isSolidGroundBlock(block)) {
                     break;
                  }
               }

               if (!flag
                  && this.recordViolation(
                     event.getPlayer().getUniqueId(),
                     2,
                     HackType.IMPOSSIBLEINTERACT,
                     event.getPlayer().getName() + " failed ImpossibleInteract: broke through a block (GhostHand)"
                  )) {
                  event.setCancelled(true);
               }
            }

            this.decreaseViolationLevel(event.getPlayer().getUniqueId(), 1);
         }
      }
   }

   @EventHandler
   public void onBlockPlace(BlockPlaceEvent event) {
      if (!this.plugin.checkManager.isPlayerExempt(event.getPlayer())) {
         Material material = event.getBlockAgainst().getType();
         if ((material == Material.LAVA || material == Material.WATER || material == Material.STATIONARY_LAVA || material == Material.STATIONARY_WATER)
            && this.cancelLiquids
            && event.getPlayer().getItemInHand().getType() != Material.WATER_LILY
            && this.recordViolation(
               event.getPlayer().getUniqueId(),
               2,
               HackType.IMPOSSIBLEINTERACT,
               event.getPlayer().getName() + " failed ImpossibleInteract: Interacted with a liquid"
            )) {
            event.setCancelled(true);
         }

         if (this.cancelGhosthand) {
            BlockIterator blockIterator;
            try {
               blockIterator = new BlockIterator(event.getPlayer(), 7);
            } catch (IllegalStateException ex) {
               return;
            }

            boolean flag = true;
            boolean flag2 = false;

            while (blockIterator.hasNext()) {
               Block block = blockIterator.next();
               if (block.equals(event.getBlockAgainst())) {
                  flag2 = true;
               }

               if (BlockCollisionChecks.isSolidGroundBlock(block) && !block.equals(event.getBlock()) && !flag2) {
                  flag = false;
               }
            }

            if (!flag) {
               event.setCancelled(true);
            }
         }
      }
   }

   @EventHandler
   public void onInventoryOpen(InventoryOpenEvent event) {
      if (this.cancelGhosthand) {
         try {
            if (this.plugin.checkManager.isPlayerExempt((Player)event.getPlayer())) {
               return;
            }

            if (event.getInventory() != null && event.getInventory().getHolder() != null) {
               if (!(event.getInventory().getHolder() instanceof Minecart) && !(event.getInventory().getHolder() instanceof LivingEntity)) {
                  if (event.getInventory().getHolder() instanceof DoubleChest) {
                     DoubleChest doubleChest = (DoubleChest)event.getInventory().getHolder();

                     BlockIterator blockIterator;
                     try {
                        blockIterator = new BlockIterator(event.getPlayer(), 7);
                     } catch (IllegalStateException ex) {
                        return;
                     }

                     boolean flag = true;
                     boolean flag2 = false;

                     while (blockIterator.hasNext()) {
                        Block block = blockIterator.next();
                        if (block.equals(((BlockState)doubleChest.getLeftSide()).getBlock()) || block.equals(((BlockState)doubleChest.getRightSide()).getBlock())) {
                           flag2 = true;
                        }

                        if (BlockCollisionChecks.isSolidGroundBlock(block) && !flag2) {
                           flag = false;
                        }
                     }

                     if (!flag
                        && this.recordViolation(
                           event.getPlayer().getUniqueId(),
                           2,
                           HackType.IMPOSSIBLEINTERACT,
                           event.getPlayer().getName() + " failed ImpossibleInteract: interacted through a block (GhostHand)"
                        )) {
                        event.setCancelled(true);
                        return;
                     }
                  } else {
                     Block block2;
                     try {
                        block2 = ((BlockState)event.getInventory().getHolder()).getBlock();
                     } catch (Exception ex2) {
                        return;
                     }

                     BlockIterator blockIterator2;
                     try {
                        blockIterator2 = new BlockIterator(event.getPlayer(), 7);
                     } catch (IllegalStateException ex3) {
                        return;
                     }

                     boolean flag3 = true;
                     boolean flag4 = false;

                     while (blockIterator2.hasNext()) {
                        Block block3 = blockIterator2.next();
                        if (block3.equals(block2)) {
                           flag4 = true;
                        }

                        if (BlockCollisionChecks.isSolidGroundBlock(block3) && !flag4) {
                           flag3 = false;
                        }
                     }

                     if (!flag3
                        && this.recordViolation(
                           event.getPlayer().getUniqueId(),
                           2,
                           HackType.IMPOSSIBLEINTERACT,
                           event.getPlayer().getName() + " failed ImpossibleInteract: interacted through a block (GhostHand)"
                        )) {
                        event.setCancelled(true);
                        return;
                     }
                  }

                  return;
               }

               return;
            }

            return;
         } catch (ClassCastException ex4) {
         }
      }
   }


   private static class VerifyLineOfSightTask implements Runnable {
      final PlayerInteractEvent event;
      final ImpossibleInteract impossibleInteract;

      VerifyLineOfSightTask(ImpossibleInteract impossibleInteract, PlayerInteractEvent event) {
         this.impossibleInteract = impossibleInteract;
         this.event = event;
      }

      @Override
      public void run() {
         BlockIterator blockIterator;
         try {
            blockIterator = new BlockIterator(this.event.getPlayer(), 7);
         } catch (IllegalStateException ex) {
            return;
         }

         boolean flag = true;
         boolean flag2 = false;

         while (blockIterator.hasNext()) {
            Block block = blockIterator.next();
            if (block.equals(this.event.getClickedBlock())) {
               flag2 = true;
            }

            if (BlockCollisionChecks.isSolidGroundBlock(block) && !flag2) {
               flag = false;
            }
         }

         if (!flag
            && flag2
            && this.impossibleInteract.recordViolation(this.event.getPlayer().getUniqueId(), 2, HackType.IMPOSSIBLEINTERACT, this.event.getPlayer().getName() + " failed ImpossibleInteract: interacted through a block (GhostHand)")) {
            this.event.getPlayer().closeInventory();
         }
      }
   }
}
