package me.konsolas.aac.detectors.combat;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import me.konsolas.aac.AAC;
import me.konsolas.aac.api.HackType;
import me.konsolas.aac.detectors.Detector;
import me.konsolas.aac.detectors.MoveListener;
import me.konsolas.aac.util.EntityTargetCategories;
import me.konsolas.aac.util.KnockbackDistanceTracker;
import me.konsolas.aac.util.Ping;
import me.konsolas.aac.util.blocks.RelativeBlockCache;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerMoveEvent;

public final class Knockback extends Detector implements MoveListener {
   private int tickDelay = 7;
   private boolean addPing = true;
   private int maxPing = 500;
   private double minKnockbackDist = 0.1;
   private ConcurrentHashMap<UUID, KnockbackDistanceTracker> trackers = new ConcurrentHashMap<>();

   public Knockback(AAC plugin) {
      super(plugin);
      this.tickDelay = this.plugin.getConfig().getInt("knockback.tick_delay", this.tickDelay);
      this.addPing = this.plugin.getConfig().getBoolean("knockback.add_ping", this.addPing);
      this.maxPing = this.plugin.getConfig().getInt("knockback.max_ping", this.maxPing);
      this.minKnockbackDist = this.plugin.getConfig().getDouble("knockback.min_knockback_dist", this.minKnockbackDist);
   }

   @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
   public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
      if (event.getEntity().getType() == EntityType.PLAYER
         && event.getEntity().getVehicle() == null
         && !this.plugin.checkManager.isPlayerExempt((Player)event.getEntity())
         && event.getCause() != DamageCause.THORNS) {
         if (this.trackers.containsKey(event.getEntity().getUniqueId())) {
            this.trackers.get(event.getEntity().getUniqueId()).cancelTask();
            this.trackers.remove(event.getEntity().getUniqueId());
         }

         if (EntityTargetCategories.isKnockbackSource(event.getDamager().getType())) {
            double d = event.getDamager().getLocation().distance(event.getEntity().getLocation());
            if (!(d < 1.2)) {
               if (event.getDamager().getType() == EntityType.ARROW) {
                  Arrow arrow = (Arrow)event.getDamager();
                  if (arrow.getShooter() instanceof LivingEntity) {
                     LivingEntity livingEntity = (LivingEntity)arrow.getShooter();
                     if (event.getEntity().getEntityId() == livingEntity.getEntityId()) {
                        return;
                     }
                  }
               }

               Player player = (Player)event.getEntity();
               RelativeBlockCache relativeBlockCache = new RelativeBlockCache(player.getLocation());
               if (!this.isExemptBlock(relativeBlockCache.getRelativeMaterial(0.6, 0.0, 0.0))
                  && !this.isExemptBlock(relativeBlockCache.getRelativeMaterial(-0.6, 0.0, 0.0))
                  && !this.isExemptBlock(relativeBlockCache.getRelativeMaterial(0.0, 0.0, 0.6))
                  && !this.isExemptBlock(relativeBlockCache.getRelativeMaterial(0.0, 0.0, -0.6))
                  && !this.isExemptBlock(relativeBlockCache.getRelativeMaterial(0.6, 0.0, 0.6))
                  && !this.isExemptBlock(relativeBlockCache.getRelativeMaterial(-0.6, 0.0, 0.6))
                  && !this.isExemptBlock(relativeBlockCache.getRelativeMaterial(0.6, 0.0, -0.6))
                  && !this.isExemptBlock(relativeBlockCache.getRelativeMaterial(-0.6, 0.0, -0.6))
                  && !this.isExemptBlock(relativeBlockCache.getRelativeMaterial(0.6, 1.1, 0.0))
                  && !this.isExemptBlock(relativeBlockCache.getRelativeMaterial(-0.6, 1.1, 0.0))
                  && !this.isExemptBlock(relativeBlockCache.getRelativeMaterial(0.0, 1.1, 0.6))
                  && !this.isExemptBlock(relativeBlockCache.getRelativeMaterial(0.0, 1.1, -0.6))
                  && !this.isExemptBlock(relativeBlockCache.getRelativeMaterial(0.6, 1.1, 0.6))
                  && !this.isExemptBlock(relativeBlockCache.getRelativeMaterial(-0.6, 1.1, 0.6))
                  && !this.isExemptBlock(relativeBlockCache.getRelativeMaterial(0.6, 1.1, -0.6))
                  && !this.isExemptBlock(relativeBlockCache.getRelativeMaterial(-0.6, 1.1, -0.6))
                  && !this.isExemptBlock(relativeBlockCache.getRelativeMaterial(0.6, 2.1, 0.0))
                  && !this.isExemptBlock(relativeBlockCache.getRelativeMaterial(-0.6, 2.1, 0.0))
                  && !this.isExemptBlock(relativeBlockCache.getRelativeMaterial(0.0, 2.1, 0.6))
                  && !this.isExemptBlock(relativeBlockCache.getRelativeMaterial(0.0, 2.1, -0.6))
                  && !this.isExemptBlock(relativeBlockCache.getRelativeMaterial(0.6, 2.1, 0.6))
                  && !this.isExemptBlock(relativeBlockCache.getRelativeMaterial(-0.6, 2.1, 0.6))
                  && !this.isExemptBlock(relativeBlockCache.getRelativeMaterial(0.6, 2.1, -0.6))
                  && !this.isExemptBlock(relativeBlockCache.getRelativeMaterial(-0.6, 2.1, -0.6))
                  && !this.isExemptBlock(relativeBlockCache.getRelativeMaterial(0.0, 1.1, 0.0))
                  && !this.isExemptBlock(relativeBlockCache.getRelativeMaterial(0.0, 2.1, 0.0))
                  && !this.isExemptBlock(relativeBlockCache.getCenterBlock().getType())) {
                  int value = this.tickDelay;
                  int value2 = Ping.getPing(player);
                  if (this.addPing) {
                     value += value2 < this.maxPing ? value2 / 100 : this.maxPing / 100;
                  }

                  KnockbackDistanceTracker knockbackDistanceTracker;
                  try {
                     knockbackDistanceTracker = new KnockbackDistanceTracker(event.getDamager(), event.getEntity(), false);
                  } catch (IllegalStateException ex) {
                     return;
                  }

                  this.trackers.put(player.getUniqueId(), knockbackDistanceTracker);
                  knockbackDistanceTracker.setTaskId(Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new EvaluateKnockbackTask(this, player), value));
               }
            }
         }
      }
   }

   @Override
   public boolean shouldProcessPlayerMove(PlayerMoveEvent event) {
      return true;
   }

   @Override
   public void onPlayerMove(PlayerMoveEvent event, boolean flag, boolean flag2, RelativeBlockCache relativeBlockCache, RelativeBlockCache relativeBlockCache2) {
      if (this.trackers.containsKey(event.getPlayer().getUniqueId())) {
         KnockbackDistanceTracker knockbackDistanceTracker = this.trackers.get(event.getPlayer().getUniqueId());
         knockbackDistanceTracker.updateLocation(event.getPlayer().getLocation());
      }
   }

   private boolean isExemptBlock(Material material) {
      return material == Material.WEB
         || material == Material.WATER
         || material == Material.STATIONARY_WATER
         || material == Material.LAVA
         || material == Material.STATIONARY_LAVA
         || material.isSolid()
         || material == Material.SKULL
         || material == Material.FLOWER_POT;
   }

   static ConcurrentHashMap getTrackers(Knockback knockback) {
      return knockback.trackers;
   }


   static double getMinKnockbackDist(Knockback knockback) {
      return knockback.minKnockbackDist;
   }


   private static class EvaluateKnockbackTask implements Runnable {
      final Player player;
      final Knockback knockback;

      EvaluateKnockbackTask(Knockback knockback, Player player) {
         this.knockback = knockback;
         this.player = player;
      }

      @Override
      public void run() {
         KnockbackDistanceTracker knockbackDistanceTracker = (KnockbackDistanceTracker)Knockback.getTrackers(this.knockback).get(this.player.getUniqueId());
         Knockback.getTrackers(this.knockback).remove(this.player.getUniqueId());
         double d = knockbackDistanceTracker.getMaxDistance() - knockbackDistanceTracker.getInitialDistance();
         knockbackDistanceTracker.getRetreatTicks();
         if (d == 0.0) {
            this.knockback.recordViolation(this.player.getUniqueId(), 2, HackType.KNOCKBACK, this.player.getName() + " failed Knockback: didn't move away from his attacker when hit");
         } else if (d < Knockback.getMinKnockbackDist(this.knockback)) {
            this.knockback.recordViolation(this.player.getUniqueId(), 2, HackType.KNOCKBACK, this.player.getName() + " failed Knockback: didn't move far enough when hit (" + d + " blocks" + (knockbackDistanceTracker.isExempt() ? ", inWater)" : ")"));
         } else {
            this.knockback.decreaseViolationLevel(this.player.getUniqueId(), 1);
         }
      }
   }
}
