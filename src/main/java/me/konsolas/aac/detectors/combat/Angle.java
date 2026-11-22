package me.konsolas.aac.detectors.combat;

import me.konsolas.aac.AAC;
import me.konsolas.aac.api.HackType;
import me.konsolas.aac.detectors.DamageListener;
import me.konsolas.aac.detectors.Detector;
import me.konsolas.aac.util.general.NumberUtil;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

public final class Angle extends Detector implements DamageListener {
   private double maxDiff = 0.2;
   private double maxPlayerDiff = 0.1;
   private boolean cancelFalseHit = true;
   private double minDist = 3.0;

   public Angle(AAC plugin) {
      super(plugin);
      this.maxDiff = this.plugin.getConfig().getDouble("angle.max_diff", this.maxDiff);
      this.maxPlayerDiff = this.plugin.getConfig().getDouble("angle.max_player_diff", this.maxPlayerDiff);
      this.cancelFalseHit = this.plugin.getConfig().getBoolean("angle.cancel_false_hit", this.cancelFalseHit);
      this.minDist = this.plugin.getConfig().getDouble("angle.min_dist", this.minDist);
   }

   public static double calculateLookAlignment(Location location, Location location2) {
      location.setY(1.0);
      location2.setY(1.0);
      Vector vector = location2.toVector().subtract(location.toVector());
      Location location3 = location.clone();
      location3.setPitch(0.0F);
      Vector vector2 = location3.getDirection();
      return vector.normalize().dot(vector2);
   }

   @Override
   public boolean shouldProcessDamage(EntityDamageByEntityEvent event) {
      return event.getCause() != DamageCause.THORNS && event.getDamager().getType() == EntityType.PLAYER;
   }

   @Override
   public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
      Player player = (Player)event.getDamager();
      Location location = player.getLocation();
      Location location2 = event.getEntity().getLocation();
      location.setY(1.0);
      location2.setY(1.0);
      double d = location.distance(location2);
      if (!(d < this.minDist)) {
         double d2 = calculateLookAlignment(location, location2);
         if (event.getEntity().getType() == EntityType.PLAYER) {
            this.plugin
               .verboseLogger
               .logPlayerDebug(
                  player, "(Angle) EntityDamageByEntityEvent dot[n:" + (1.0 - this.maxPlayerDiff) + ",m" + (1.0 + this.maxPlayerDiff) + ",r:" + d2 + "]"
               );
            if (!NumberUtil.isWithinRange(d2, 1.0, this.maxPlayerDiff * 4.0 / d)) {
               if (this.recordViolation(
                     player.getUniqueId(), 2, HackType.ANGLE, player.getName() + " failed Angle: tried to attack an entity out of his line of sight"
                  )
                  && this.cancelFalseHit) {
                  event.setCancelled(true);
               }
            } else {
               this.decreaseViolationLevel(player.getUniqueId(), 1);
            }
         } else {
            this.plugin
               .verboseLogger
               .logPlayerDebug(player, "(Angle) EntityDamageByEntityEvent dot[n:" + (1.0 - this.maxDiff) + ",m" + (1.0 + this.maxDiff) + ",r:" + d2 + "]");
            if (!NumberUtil.isWithinRange(d2, 1.0, this.maxDiff)) {
               if (this.recordViolation(
                     player.getUniqueId(), 2, HackType.ANGLE, player.getName() + " failed Angle: tried to attack an entity out of his line of sight"
                  )
                  && this.cancelFalseHit) {
                  event.setCancelled(true);
               }
            } else {
               this.decreaseViolationLevel(player.getUniqueId(), 1);
            }
         }
      }
   }
}
