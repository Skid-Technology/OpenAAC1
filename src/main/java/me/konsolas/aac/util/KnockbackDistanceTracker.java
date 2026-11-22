package me.konsolas.aac.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public class KnockbackDistanceTracker {
   public static final double MAX_REBOUND_THRESHOLD = 1.2;
   private final Location startLocation;
   private final Vector initialVelocity;
   private final double initialDistance;
   private double previousDistance;
   private double maxDistance;
   private int retreatTicks;
   private boolean movingAway;
   private boolean stopped;
   private boolean exempt;
   private int taskId;

   public KnockbackDistanceTracker(Entity entity, Entity entity2, boolean flag) {
      if (!entity.getWorld().equals(entity2.getWorld())) {
         throw new IllegalStateException("Attacker and defender are in different worlds");
      }

      this.startLocation = entity.getLocation();
      this.initialDistance = Math.sqrt(
         Math.pow(entity.getLocation().getX() - entity2.getLocation().getX(), 2.0) + Math.pow(entity.getLocation().getZ() - entity2.getLocation().getZ(), 2.0)
      );
      this.initialVelocity = entity2.getVelocity();
      this.maxDistance = this.initialDistance;
      this.retreatTicks = 0;
      this.movingAway = false;
      this.exempt = flag;
   }

   public Location getStartLocation() {
      return this.startLocation;
   }

   public double getMaxDistance() {
      return this.maxDistance;
   }

   public void setTaskId(int i) {
      this.taskId = i;
   }

   public void cancelTask() {
      Bukkit.getServer().getScheduler().cancelTask(this.taskId);
   }

   public void updateLocation(Location location) {
      double x = Math.sqrt(Math.pow(this.startLocation.getX() - location.getX(), 2.0) + Math.pow(this.startLocation.getZ() - location.getZ(), 2.0));
      if (x > this.maxDistance) {
         this.maxDistance = x;
      }

      if (x < this.previousDistance && x < 1.2) {
         this.stopped = true;
      }

      boolean flag = x < this.previousDistance;
      this.updateDirectionState(flag);
      this.previousDistance = x;
   }

   private void updateDirectionState(boolean flag) {
      if (!this.stopped) {
         if (flag) {
            if (this.movingAway) {
               this.stopped = true;
            }
         } else if (this.movingAway) {
            this.retreatTicks++;
         } else {
            this.movingAway = true;
         }
      }
   }

   public int getRetreatTicks() {
      return this.retreatTicks;
   }

   public double getInitialDistance() {
      return this.initialDistance;
   }

   public Vector getInitialVelocity() {
      return this.initialVelocity;
   }

   public boolean isExempt() {
      return this.exempt;
   }
}
