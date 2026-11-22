package me.konsolas.aac.detectors.world;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import me.konsolas.aac.AAC;
import me.konsolas.aac.api.HackType;
import me.konsolas.aac.detectors.Detector;
import me.konsolas.aac.util.blocks.BlockUtil;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public final class FastBreak extends Detector {
   private int letTicks = 2;
   private boolean cancelBreak = true;
   private boolean checkDelay = true;
   private Map<UUID, Long> breakStartTimes = new HashMap<>();
   private Map<UUID, Block> targetBlocks = new HashMap<>();
   private Map<UUID, Long> lastBreakTimes = new HashMap<>();

   public FastBreak(AAC plugin) {
      super(plugin);
      this.letTicks = this.plugin.getConfig().getInt("fastbreak.let_ticks", this.letTicks);
      this.cancelBreak = this.plugin.getConfig().getBoolean("fastbreak.cancel_break", this.cancelBreak);
      this.checkDelay = this.plugin.getConfig().getBoolean("fastbreak.check_delay", this.checkDelay);
   }

   @EventHandler
   public void onPlayerInteract(PlayerInteractEvent event) {
      if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
         if (event.getClickedBlock().equals(this.targetBlocks.get(event.getPlayer().getUniqueId()))) {
            long now = System.currentTimeMillis();
            long ticks = this.breakStartTimes.get(event.getPlayer().getUniqueId());
            long ticks2 = now - ticks / 50L;
            if (ticks2 >= BlockUtil.getBreakTicks(event.getClickedBlock().getType(), event.getPlayer())) {
               this.breakStartTimes.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
            }
         } else {
            this.targetBlocks.put(event.getPlayer().getUniqueId(), event.getClickedBlock());
            this.breakStartTimes.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
         }

         if (this.checkDelay && this.lastBreakTimes.containsKey(event.getPlayer().getUniqueId()) && BlockUtil.getHasteLevel(event.getPlayer()) == 0) {
            long ticks3 = this.lastBreakTimes.get(event.getPlayer().getUniqueId());
            long now2 = System.currentTimeMillis();
            if (now2 - ticks3 < 100L
               && event.getClickedBlock() != null
               && BlockUtil.getBreakTicks(event.getClickedBlock().getType(), event.getPlayer()) >= 1L
               && this.recordViolation(
                  event.getPlayer().getUniqueId(),
                  2,
                  HackType.FASTBREAK,
                  event.getPlayer().getName() + " failed FastBreak: interacted too quickly after breaking a block"
               )
               && this.cancelBreak) {
               event.setCancelled(true);
            }
         }
      }
   }

   @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
   public void onBlockBreak(BlockBreakEvent event) {
      if (event.getPlayer().getGameMode() != GameMode.CREATIVE && !this.plugin.checkManager.isPlayerExempt(event.getPlayer())) {
         if (!event.getBlock().equals(this.targetBlocks.get(event.getPlayer().getUniqueId()))
            && this.recordViolation(
               event.getPlayer().getUniqueId(), 1, HackType.FASTBREAK, event.getPlayer().getName() + " failed FastBreak: broke the wrong block"
            )
            && this.cancelBreak) {
            event.setCancelled(true);
         }

         if (this.breakStartTimes.containsKey(event.getPlayer().getUniqueId())) {
            long now = System.currentTimeMillis();
            long ticks = this.breakStartTimes.get(event.getPlayer().getUniqueId());
            long ticks2 = (now - ticks) / 50L;
            long ticks3 = BlockUtil.getBreakTicks(event.getBlock().getType(), event.getPlayer());
            if (ticks3 == 0L) {
               return;
            }

            if (ticks2 + this.letTicks < ticks3) {
               if (this.recordViolation(
                     event.getPlayer().getUniqueId(),
                     1,
                     HackType.FASTBREAK,
                     event.getPlayer().getName() + " failed FastBreak: tried to break blocks too quickly (" + ticks2 + ", expected " + ticks3 + ")"
                  )
                  && this.cancelBreak) {
                  event.setCancelled(true);
               }
            } else {
               this.decreaseViolationLevel(event.getPlayer().getUniqueId(), 1);
            }
         }

         this.lastBreakTimes.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
      }
   }
}
