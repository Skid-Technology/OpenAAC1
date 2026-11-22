package me.konsolas.aac.detectors.combat;

import me.konsolas.aac.AAC;
import me.konsolas.aac.api.HackType;
import me.konsolas.aac.detectors.Detector;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemConsumeEvent;

public final class FastUse extends Detector {
   private int msToUse = 2700;

   public FastUse(AAC plugin) {
      super(plugin);
      this.msToUse = this.plugin.getConfig().getInt("fastuse.ms_to_use");
   }

   @EventHandler
   public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
      if (!this.plugin.checkManager.isPlayerExempt(event.getPlayer())) {
         long now = System.currentTimeMillis();
         long ticks = now - this.plugin.playerState.getLastInteractionTime(event.getPlayer());
         if (ticks < this.msToUse) {
            if (this.recordViolation(
               event.getPlayer().getUniqueId(),
               1,
               HackType.FASTUSE,
               event.getPlayer().getName() + " failed FastUse: Tried to consume an item too quickly (" + ticks + "ms)"
            )) {
               event.setCancelled(true);
               event.getPlayer().getInventory().setHeldItemSlot((event.getPlayer().getInventory().getHeldItemSlot() + 1) % 9);
            }
         } else {
            this.decreaseViolationLevel(event.getPlayer().getUniqueId(), 1);
         }
      }
   }
}
