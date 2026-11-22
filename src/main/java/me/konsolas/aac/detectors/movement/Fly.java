package me.konsolas.aac.detectors.movement;

import com.comphenix.protocol.PacketType.Play.Client;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import me.konsolas.aac.AAC;
import me.konsolas.aac.api.HackType;
import me.konsolas.aac.detectors.Detector;
import me.konsolas.aac.detectors.MoveListener;
import me.konsolas.aac.util.blocks.BlockCollisionChecks;
import me.konsolas.aac.util.blocks.RelativeBlockCache;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class Fly extends Detector implements MoveListener {
   private double maxJump = 1.4;
   private double slimeAllow = 60.0;
   private double knockbackAllow = 3.1;
   private double velocityMultiplier = 1.2;
   private double allow = 0.8;
   private double naturalAllow = 2.1;
   private boolean teleport = true;
   private Map<UUID, Double> airborneTime = new ConcurrentHashMap<>();
   private Set<UUID> recentFlightToggles = new HashSet<>();
   private Set<UUID> exemptPlayers = new HashSet<>();
   private ExpiringMap<UUID, Boolean> slimeLaunchedPlayers;

   public Fly(AAC plugin) {
      super(plugin);
      this.maxJump = this.plugin.getConfig().getDouble("fly.max_jump", this.maxJump);
      this.slimeAllow = this.plugin.getConfig().getDouble("fly.slime_allow", this.slimeAllow);
      this.knockbackAllow = this.plugin.getConfig().getDouble("fly.knockback_allow", this.knockbackAllow);
      this.velocityMultiplier = this.plugin.getConfig().getDouble("fly.velocity_multiplier", this.velocityMultiplier);
      this.allow = this.plugin.getConfig().getDouble("fly.allow", this.allow);
      this.naturalAllow = this.plugin.getConfig().getDouble("fly.natural_allow", this.naturalAllow);
      this.teleport = this.plugin.getConfig().getBoolean("fly.teleport", this.teleport);
      this.scheduledTaskIds.add(Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, new DecayViolationsTask(this), 20L, 50L));
      this.slimeLaunchedPlayers = ExpiringMap.builder().expiration(300L, TimeUnit.MILLISECONDS).expirationPolicy(ExpirationPolicy.CREATED).build();
   }

   @EventHandler
   public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
      this.recentFlightToggles.add(event.getPlayer().getUniqueId());
      Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new ClearFlightToggleTask(this, event), 50L);
   }

   public boolean isSlimeLaunched(Player player) {
      return this.slimeLaunchedPlayers.containsKey(player.getUniqueId());
   }

   @Override
   public boolean shouldProcessPlayerMove(PlayerMoveEvent event) {
      if (!event.getPlayer().isDead()
         && !this.recentFlightToggles.contains(event.getPlayer().getUniqueId())
         && !event.getPlayer().getGameMode().equals(GameMode.SPECTATOR)
         && !event.getPlayer().isFlying()) {
         if (event.getPlayer().getVehicle() != null) {
            return false;
         }

         for (Entity entity : event.getPlayer().getNearbyEntities(3.0, 3.0, 3.0)) {
            if (entity instanceof Boat) {
               return false;
            }
         }

         return !event.getPlayer().getAllowFlight();
      } else {
         return false;
      }
   }

   @Override
   public void onPlayerMove(PlayerMoveEvent event, boolean flag, boolean flag2, RelativeBlockCache relativeBlockCache, RelativeBlockCache relativeBlockCache2) {
      int value = -1;
      if (event.getPlayer().hasPotionEffect(PotionEffectType.JUMP)) {
         for (PotionEffect potionEffect : event.getPlayer().getActivePotionEffects()) {
            if (potionEffect.getType().equals(PotionEffectType.JUMP)) {
               value = potionEffect.getAmplifier();
            }
         }
      }

      Player player = event.getPlayer();
      if (!this.airborneTime.containsKey(event.getPlayer().getUniqueId())) {
         this.airborneTime.put(event.getPlayer().getUniqueId(), this.maxJump);
      }

      if (this.plugin.combatLogoutListener.hasRecentDamage(player) && this.airborneTime.get(player.getUniqueId()) < this.knockbackAllow) {
         if (this.plugin.combatLogoutListener.getLastDamageCause(player) == DamageCause.ENTITY_ATTACK || this.plugin.combatLogoutListener.getLastDamageCause(player) == DamageCause.PROJECTILE) {
            this.airborneTime.put(player.getUniqueId(), this.knockbackAllow);
         } else if (this.airborneTime.get(player.getUniqueId()) < this.naturalAllow) {
            this.airborneTime.put(player.getUniqueId(), this.naturalAllow);
         }
      }

      if (value > -1) {
         double d = Math.pow(value + 1 + 4.2, 2.0) / 16.0 + this.maxJump;
         this.airborneTime.put(player.getUniqueId(), d > this.airborneTime.get(player.getUniqueId()) ? d : this.airborneTime.get(player.getUniqueId()));
      }

      if (BlockCollisionChecks.isAboveBounceBlock(relativeBlockCache)) {
         this.airborneTime.put(event.getPlayer().getUniqueId(), this.slimeAllow);
         this.exemptPlayers.add(player.getUniqueId());
      }

      if (this.exemptPlayers.contains(event.getPlayer().getUniqueId())) {
         if (flag) {
            this.exemptPlayers.remove(event.getPlayer().getUniqueId());
         }

         this.airborneTime.put(event.getPlayer().getUniqueId(), this.slimeAllow);
      }

      double y = event.getTo().getY() - this.plugin.movementDispatcher.getSafeLocation(player).getY();
      double d2 = y - this.airborneTime.get(player.getUniqueId());
      if (d2 > 0.0) {
         this.plugin
            .verboseLogger
            .logPlayerDebug(
               player,
               "(Fly) PlayerMoveEvent from "
                  + event.getFrom().getX()
                  + "x"
                  + event.getFrom().getY()
                  + "y"
                  + event.getFrom().getZ()
                  + "z"
                  + " to "
                  + event.getTo().getX()
                  + "x"
                  + event.getTo().getY()
                  + "y"
                  + event.getTo().getZ()
                  + "z"
                  + " ground[u:"
                  + this.plugin.movementDispatcher.getSafeLocation(event.getPlayer()).getY()
                  + "] height[t:"
                  + this.airborneTime.get(player.getUniqueId())
                  + ",r:"
                  + y
                  + "]"
            );
         if (this.recordViolation(
               player.getUniqueId(),
               (int)((long)d2 > 100L ? 100L : 1L + (long)d2),
               HackType.FLY,
               player.getName() + " failed Fly: tried to jump too high (" + y + " blocks)"
            )
            && (d2 > this.allow || this.getViolationLevel(player) > 2)
            && this.teleport) {
            this.slimeLaunchedPlayers.putIfAbsent(player.getUniqueId(), false);
            this.slimeLaunchedPlayers.resetExpiration(player.getUniqueId());
            float f = player.getFallDistance();
            this.plugin.movementDispatcher.setTeleportExempt(this, player);
            Location location = this.plugin.movementDispatcher.getSafeLocation(player);
            player.teleport(location, TeleportCause.UNKNOWN);
            this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new DeferredFallSetbackTask(this, player, location, f, event));
         }
      }

      if (flag2) {
         this.airborneTime.put(event.getPlayer().getUniqueId(), this.maxJump);
      }
   }

   @EventHandler
   public void onPlayerVelocity(PlayerVelocityEvent event) {
      if (!(event.getVelocity().getY() <= 0.0)) {
         if (!this.airborneTime.containsKey(event.getPlayer().getUniqueId())) {
            this.airborneTime.put(event.getPlayer().getUniqueId(), this.maxJump);
         }

         double y = this.predictMaxJumpHeight(event.getVelocity().getY());
         double y2 = event.getPlayer().getLocation().getY() - this.plugin.movementDispatcher.getSafeLocation(event.getPlayer()).getY();
         if (y + y2 > this.airborneTime.get(event.getPlayer().getUniqueId())) {
            this.airborneTime.put(event.getPlayer().getUniqueId(), (y + y2) * this.velocityMultiplier);
         }

         this.plugin
            .verboseLogger
            .logPlayerDebug(
               event.getPlayer(),
               "(Fly) PlayerVelocityEvent velocityY[r:" + event.getVelocity().getY() + "] newHeight[u:" + this.airborneTime.get(event.getPlayer().getUniqueId()) + "]"
            );
      }
   }

   private double predictMaxJumpHeight(double d) {
      double d2 = 0.0;
      double d3 = 0.0;
      int value = 0;

      do {
         d2 += d3;
         d3 = (d + 4.0) * StrictMath.exp(-0.02 * value) - 4.0;
         value++;
      } while (d3 > 0.0);

      return d2;
   }

   @EventHandler(priority = EventPriority.MONITOR)
   public void onFlyPlayerJoin(PlayerJoinEvent event) {
      this.violationLevels.put(event.getPlayer().getUniqueId(), 0);
      this.airborneTime.put(event.getPlayer().getUniqueId(), this.maxJump);
      Bukkit.getPluginManager().callEvent(new PlayerVelocityEvent(event.getPlayer(), event.getPlayer().getVelocity()));
   }

   @Override
   public void destroy() {
      super.destroy();
   }

   public void clearCaches() {
      this.exemptPlayers.clear();
      this.airborneTime.clear();
      this.slimeLaunchedPlayers.clear();
      this.recentFlightToggles.clear();
   }


   static Set getRecentFlightToggles(Fly fly) {
      return fly.recentFlightToggles;
   }


   private static class DecayViolationsTask implements Runnable {
      final Fly fly;

      DecayViolationsTask(Fly fly) {
         this.fly = fly;
      }

      @Override
      public void run() {
         for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            this.fly.decreaseViolationLevel(player.getUniqueId(), 1);
         }
      }
   }

   private static class ClearFlightToggleTask implements Runnable {
      final PlayerToggleFlightEvent event;
      final Fly fly;

      ClearFlightToggleTask(Fly fly, PlayerToggleFlightEvent event) {
         this.fly = fly;
         this.event = event;
      }

      @Override
      public void run() {
         Fly.getRecentFlightToggles(this.fly).remove(this.event.getPlayer().getUniqueId());
      }
   }

   private static class ApplyFallDamageTask implements Runnable {
      final PacketContainer packet;
      final DeferredFallSetbackTask setbackTask;

      ApplyFallDamageTask(DeferredFallSetbackTask deferredFallSetbackTask, PacketContainer packet) {
         this.setbackTask = deferredFallSetbackTask;
         this.packet = packet;
      }

      @Override
      public void run() {
         this.setbackTask.player.setFallDistance(this.setbackTask.fallDistance);

         try {
            ProtocolLibrary.getProtocolManager().recieveClientPacket(this.setbackTask.event.getPlayer(), this.packet, false);
         } catch (Exception ex) {
            this.setbackTask.fly.getPlugin().verboseLogger.logViolation("(Fly) Failed to deal fall damage to player: " + ex.getMessage());
         }
      }
   }

   private static class DeferredFallSetbackTask implements Runnable {
      final Player player;
      final Location location;
      final float fallDistance;
      final PlayerMoveEvent event;
      final Fly fly;

      DeferredFallSetbackTask(Fly fly, Player player, Location location, float f, PlayerMoveEvent event) {
         this.fly = fly;
         this.player = player;
         this.location = location;
         this.fallDistance = f;
         this.event = event;
      }

      @Override
      public void run() {
         this.fly.getPlugin().movementDispatcher.setTeleportExempt(this.fly, this.player);
         this.player.teleport(this.location, TeleportCause.UNKNOWN);
         PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(Client.FLYING);
         packet.getBooleans().write(0, true);
         Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.fly.getPlugin(), new ApplyFallDamageTask(this, packet), 2L);
      }
   }
}
