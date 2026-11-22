package me.konsolas.aac.detectors.combat;

import me.konsolas.aac.AAC;
import me.konsolas.aac.api.HackType;
import me.konsolas.aac.detectors.DamageListener;
import me.konsolas.aac.detectors.Detector;
import me.konsolas.aac.util.general.PlayerItemUseReflection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public final class CombatImpossible extends Detector implements DamageListener {
   private boolean cancelDead = true;
   private boolean cancelUsingItem = true;

   public CombatImpossible(AAC plugin) {
      super(plugin);
      this.cancelDead = this.plugin.getConfig().getBoolean("combatimpossible.cancel_dead", this.cancelDead);
      this.cancelUsingItem = this.plugin.getConfig().getBoolean("combatimpossible.cancel_using_item", this.cancelUsingItem);
   }

   @Override
   public boolean shouldProcessDamage(EntityDamageByEntityEvent event) {
      return event.getDamager().getType() == EntityType.PLAYER;
   }

   @Override
   public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
      Player player = (Player)event.getDamager();
      if (player.isDead() && this.cancelDead) {
         if (this.recordViolation(player.getUniqueId(), 1, HackType.COMBATIMPOSSIBLE, player.getName() + " failed CombatImpossible: Tried to attack whilst dead")) {
            event.setCancelled(true);
         }
      } else if (PlayerItemUseReflection.isUsingItem(player)
         && this.cancelUsingItem
         && this.recordViolation(
            player.getUniqueId(), 1, HackType.COMBATIMPOSSIBLE, player.getName() + " failed CombatImpossible: Tried to attack whilst using an item"
         )) {
         event.setCancelled(true);
      }
   }
}
