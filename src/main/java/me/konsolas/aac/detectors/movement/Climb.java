package me.konsolas.aac.detectors.movement;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import me.konsolas.aac.AAC;
import me.konsolas.aac.api.HackType;
import me.konsolas.aac.detectors.DamageListener;
import me.konsolas.aac.detectors.Detector;
import me.konsolas.aac.detectors.MoveListener;
import me.konsolas.aac.util.blocks.BlockCollisionChecks;
import me.konsolas.aac.util.blocks.RelativeBlockCache;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public final class Climb extends Detector implements MoveListener, DamageListener {
   private double maxAscentSpeed = 0.15;
   private double maxStartSpeed = 0.6;
   private boolean teleport = true;
   private Map<UUID, Double> climbStartHeights = new HashMap<>();

   public Climb(AAC plugin) {
      super(plugin);
      this.maxAscentSpeed = this.plugin.getConfig().getDouble("climb.max_ascent_speed", this.maxAscentSpeed);
      this.maxStartSpeed = this.plugin.getConfig().getDouble("climb.max_start_speed", this.maxStartSpeed);
      this.teleport = this.plugin.getConfig().getBoolean("climb.teleport", this.teleport);
   }

   @Override
   public boolean shouldProcessPlayerMove(PlayerMoveEvent event) {
      if (event.getPlayer().isFlying() || event.getPlayer().getVehicle() != null) {
         return false;
      } else if (!BlockCollisionChecks.isOnClimbable(event.getPlayer())) {
         this.climbStartHeights.remove(event.getPlayer().getUniqueId());
         return false;
      } else {
         return true;
      }
   }

   @Override
   public boolean shouldProcessDamage(EntityDamageByEntityEvent event) {
      return true;
   }

   @Override
   public void onPlayerMove(PlayerMoveEvent event, boolean flag, boolean flag2, RelativeBlockCache relativeBlockCache, RelativeBlockCache relativeBlockCache2) {
      if (flag && flag2) {
         double y = event.getFrom().getY();
         double y2 = event.getTo().getY();
         double d = y2 - y;
         if (d > 0.0) {
            if (d > this.maxStartSpeed) {
               if (this.recordViolation(
                     event.getPlayer().getUniqueId(), 2, HackType.CLIMB, event.getPlayer().getName() + " failed Climb: initiated a ladder climb too quickly"
                  )
                  && this.teleport) {
                  event.setTo(event.getFrom());
               }
            } else if (!this.climbStartHeights.containsKey(event.getPlayer().getUniqueId())
               || !(event.getPlayer().getLocation().getY() > this.climbStartHeights.get(event.getPlayer().getUniqueId()) + this.maxStartSpeed)) {
               this.decreaseViolationLevel(event.getPlayer().getUniqueId(), 1);
            } else if (d > this.maxAscentSpeed
               && this.recordViolation(
                  event.getPlayer().getUniqueId(), 2, HackType.CLIMB, event.getPlayer().getName() + " failed Climb: climbed a ladder too quickly"
               )
               && this.teleport) {
               event.setTo(event.getFrom());
            }

            if (!this.climbStartHeights.containsKey(event.getPlayer().getUniqueId())) {
               this.climbStartHeights.put(event.getPlayer().getUniqueId(), event.getPlayer().getLocation().getY());
            }
         } else {
            this.climbStartHeights.remove(event.getPlayer().getUniqueId());
         }
      }
   }

   @Override
   public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
      this.climbStartHeights.remove(event.getEntity().getUniqueId());
   }
}
