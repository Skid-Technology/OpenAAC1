package me.konsolas.aac.detectors.world;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import me.konsolas.aac.AAC;
import me.konsolas.aac.api.HackType;
import me.konsolas.aac.detectors.Detector;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public final class FastPlace extends Detector {
   private long minDelay = 200L;
   private boolean cancelPlace = true;
   private Map<UUID, Long> lastPlaceTimes = new HashMap<>();

   public FastPlace(AAC plugin) {
      super(plugin);
      this.minDelay = this.plugin.getConfig().getLong("fastplace.min_delay", this.minDelay);
      this.cancelPlace = this.plugin.getConfig().getBoolean("fastplace.cancel_place", this.cancelPlace);
   }

   @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
   public void onBlockPlace(BlockPlaceEvent event) {
      if (event.getPlayer().getItemInHand().getType() != Material.FLINT_AND_STEEL
         && event.getPlayer().getGameMode() != GameMode.CREATIVE
         && !this.plugin.checkManager.isPlayerExempt(event.getPlayer())) {
         if (!event.getPlayer().getItemInHand().isSimilar(new ItemStack(Material.INK_SACK, 1, (short)15))) {
            if (this.lastPlaceTimes.containsKey(event.getPlayer().getUniqueId())) {
               long now = System.currentTimeMillis();
               long ticks = this.lastPlaceTimes.get(event.getPlayer().getUniqueId());
               if (now - ticks < this.minDelay) {
                  if (this.recordViolation(
                        event.getPlayer().getUniqueId(),
                        2,
                        HackType.FASTBREAK,
                        event.getPlayer().getName() + " failed FastPlace: tried to place blocks too quickly (" + (now - ticks) + " ms)"
                     )
                     && this.cancelPlace) {
                     event.setCancelled(true);
                  }
               } else {
                  this.decreaseViolationLevel(event.getPlayer().getUniqueId(), 1);
               }
            }

            this.lastPlaceTimes.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
         }
      }
   }
}
