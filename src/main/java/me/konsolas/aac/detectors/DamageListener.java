package me.konsolas.aac.detectors;

import org.bukkit.event.entity.EntityDamageByEntityEvent;

public interface DamageListener {
   void onEntityDamageByEntity(EntityDamageByEntityEvent event);

   boolean shouldProcessDamage(EntityDamageByEntityEvent event);
}
