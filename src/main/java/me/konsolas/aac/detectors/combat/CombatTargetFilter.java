package me.konsolas.aac.detectors.combat;

import me.konsolas.aac.AAC;
import me.konsolas.aac.detectors.DamageListener;
import me.konsolas.aac.detectors.Detector;
import me.konsolas.aac.util.EntityTargetCategories;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class CombatTargetFilter extends Detector implements DamageListener {
   public CombatTargetFilter(AAC plugin) {
      super(plugin);
   }

   @Override
   public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
   }

   @Override
   public boolean shouldProcessDamage(EntityDamageByEntityEvent event) {
      return event.getDamager() instanceof Player && EntityTargetCategories.isKillauraTarget(event.getEntityType()) && EntityTargetCategories.isReachTarget(event.getEntityType());
   }
}
