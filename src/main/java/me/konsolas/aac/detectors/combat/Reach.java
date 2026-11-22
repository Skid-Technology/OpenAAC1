package me.konsolas.aac.detectors.combat;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import me.konsolas.aac.AAC;
import me.konsolas.aac.api.HackType;
import me.konsolas.aac.detectors.DamageListener;
import me.konsolas.aac.detectors.Detector;
import me.konsolas.aac.detectors.MoveListener;
import me.konsolas.aac.util.EntityTargetCategories;
import me.konsolas.aac.util.blocks.RelativeBlockCache;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerMoveEvent;

public final class Reach extends Detector implements DamageListener, MoveListener {
   private double maxReach = 4.0;
   private double maxBukkitReach = 9.0;
   private double moveMultiplier = 3.0;
   public Map<UUID, PlayerMoveEvent> lastMoveEvents = new HashMap<>();

   public Reach(AAC plugin) {
      super(plugin);
      this.maxReach = this.plugin.getConfig().getDouble("reach.max_reach", this.maxReach);
      this.maxBukkitReach = this.plugin.getConfig().getDouble("reach.max_bukkit_reach", this.maxBukkitReach);
      this.moveMultiplier = this.plugin.getConfig().getDouble("reach.move_multiplier", this.moveMultiplier);
   }

   @Override
   public boolean shouldProcessDamage(EntityDamageByEntityEvent event) {
      return event.getDamager().getType() == EntityType.PLAYER
         && !event.getCause().equals(DamageCause.THORNS)
         && EntityTargetCategories.isReachTarget(event.getEntity().getType());
   }

   @Override
   public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
      if (event.getEntity() instanceof LivingEntity) {
         Location location = event.getDamager().getLocation().add(0.0, 1.5, 0.0);
         Location location2 = event.getEntity().getLocation();
         Location location3 = event.getEntity().getLocation().add(0.0, ((LivingEntity)event.getEntity()).getEyeHeight(), 0.0);
         double d = Math.min(location.distance(location2), location.distance(location3));
         this.plugin.verboseLogger.logPlayerDebug((Player)event.getDamager(), "(Reach) EntityDamageByEntityEvent dist[m:" + this.maxReach + ",r:" + d + "]");
         if (!(d > this.maxReach) || !(d < this.maxBukkitReach)) {
            this.decreaseViolationLevel(event.getDamager().getUniqueId(), 1);
         } else if (event.getEntity() instanceof Player) {
            PlayerMoveEvent event2 = this.lastMoveEvents.get(event.getEntity().getUniqueId());
            double d2;
            if (event2 != null) {
               d2 = event2.getFrom().distance(event2.getTo()) * this.moveMultiplier;
            } else {
               d2 = 0.0;
            }

            if (d > this.maxReach + d2
               && this.recordViolation(
                  event.getDamager().getUniqueId(),
                  2,
                  HackType.REACH,
                  event.getDamager().getName() + " failed Reach: Tried to hit a player " + d + " blocks away"
               )) {
               event.setCancelled(true);
            }
         } else if (d > this.maxReach + 1.2
            && this.recordViolation(
               event.getDamager().getUniqueId(),
               1,
               HackType.REACH,
               event.getDamager().getName() + " failed Reach: Tried to hit an entity " + d + " blocks away"
            )) {
            event.setCancelled(true);
         }
      }
   }

   @Override
   public void onPlayerMove(PlayerMoveEvent event, boolean flag, boolean flag2, RelativeBlockCache relativeBlockCache, RelativeBlockCache relativeBlockCache2) {
      this.lastMoveEvents.put(event.getPlayer().getUniqueId(), new PlayerMoveEvent(event.getPlayer(), event.getFrom(), event.getTo()));
   }

   @Override
   public boolean shouldProcessPlayerMove(PlayerMoveEvent event) {
      return true;
   }
}
