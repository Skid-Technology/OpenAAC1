package me.konsolas.aac.detectors.combat;

import me.konsolas.aac.AAC;
import me.konsolas.aac.api.HackType;
import me.konsolas.aac.detectors.DamageListener;
import me.konsolas.aac.detectors.Detector;
import me.konsolas.aac.util.blocks.BlockCollisionChecks;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffectType;

public final class Criticals extends Detector implements DamageListener {
   private boolean cancelFalseHit = false;

   public Criticals(AAC plugin) {
      super(plugin);
      this.cancelFalseHit = this.plugin.getConfig().getBoolean("criticals.cancel_false_hit", this.cancelFalseHit);
   }

   @Override
   public boolean shouldProcessDamage(EntityDamageByEntityEvent event) {
      return event.getDamager().getType() == EntityType.PLAYER && !((Player)event.getDamager()).isFlying();
   }

   @Override
   public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
      Player player = (Player)event.getDamager();
      if (this.isCriticalCandidate(player)) {
         this.plugin
            .verboseLogger
            .logPlayerDebug(
               player,
               "(Criticals) EntityDamageByEntityEvent critical location[r:"
                  + player.getLocation()
                  + "] velocity[r:"
                  + player.getVelocity()
                  + "] blockOn[r:"
                  + player.getLocation().subtract(0.0, 1.0, 0.0).getBlock().getType()
                  + "]"
            );
         if ((
               player.getLocation().getY() % 1.0 == 0.0 && BlockCollisionChecks.isSolidGroundBlock(player.getLocation().subtract(0.0, 1.0, 0.0).getBlock())
                  || player.getLocation().getY() % 0.5 == 0.0
                     && BlockCollisionChecks.isSlab(player.getLocation().getBlock().getType())
                     && !this.plugin.checkManager.isPlayerExempt(player)
            )
            && this.recordViolation(
               player.getUniqueId(), 2, HackType.CRITICALS, player.getName() + " failed Criticals: tried to perform a critical hit whilst on the ground"
            )
            && this.cancelFalseHit) {
            event.setCancelled(true);
         }
      } else {
         this.plugin
            .verboseLogger
            .logPlayerDebug(
               player,
               "(Criticals) EntityDamageByEntityEvent !critical location[r:"
                  + player.getLocation()
                  + "] velocity[r:"
                  + player.getVelocity()
                  + "] blockOn[r:"
                  + player.getLocation().subtract(0.0, 1.0, 0.0).getBlock().getType()
                  + "]"
            );
         if (player.getLocation().getY() % 1.0 == 0.0 && BlockCollisionChecks.isSolidGroundBlock(player.getLocation().subtract(0.0, 1.0, 0.0).getBlock())) {
            this.decreaseViolationLevel(player.getUniqueId(), 1);
         }
      }
   }

   private boolean isCriticalCandidate(Player player) {
      return player.getFallDistance() > 0.0F
         && !player.isOnGround()
         && !player.isInsideVehicle()
         && !player.hasPotionEffect(PotionEffectType.BLINDNESS)
         && !BlockCollisionChecks.isOnClimbable(player)
         && !BlockCollisionChecks.isInLiquid(player);
   }
}
