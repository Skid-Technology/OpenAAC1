package me.konsolas.aac.detectors.combat;

import me.konsolas.aac.AAC;
import me.konsolas.aac.api.HackType;
import me.konsolas.aac.detectors.Detector;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.entity.EntityRegainHealthEvent;

public final class Regen extends Detector {
   private long minRegenDelay = 3850L;
   private boolean cancelRegen = true;

   public Regen(AAC plugin) {
      super(plugin);
      this.cancelRegen = this.plugin.getConfig().getBoolean("regen.cancel_regen", this.cancelRegen);
      this.minRegenDelay = this.plugin.getConfig().getLong("regen.min_regen_delay", this.minRegenDelay);
   }

   @EventHandler
   public void onEntityRegainHealth(EntityRegainHealthEvent event) {
      if (event.getEntity().getType() == EntityType.PLAYER && !this.plugin.checkManager.isPlayerExempt((Player)event.getEntity())) {
         Player player = (Player)event.getEntity();
         long ticks = this.plugin.playerState.getLastRegenerationTime(player);
         RegainReason regainReason = this.plugin.playerState.getLastRegenerationReason(player);
         long now = System.currentTimeMillis() - ticks;
         if (regainReason == RegainReason.SATIATED && event.getRegainReason() == RegainReason.SATIATED) {
            if (now < this.minRegenDelay) {
               if (this.recordViolation(player.getUniqueId(), 1, HackType.REGEN, player.getName() + " failed Regen: tried to regenerate health faster than normal")
                  && this.cancelRegen) {
                  event.setCancelled(true);
               }
            } else {
               this.decreaseViolationLevel(player.getUniqueId(), 1);
            }
         }
      }
   }
}
