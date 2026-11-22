package me.konsolas.aac.detectors.movement;

import com.comphenix.protocol.PacketType.Play.Client;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import me.konsolas.aac.AAC;
import me.konsolas.aac.api.HackType;
import me.konsolas.aac.detectors.Detector;
import me.konsolas.aac.detectors.MoveListener;
import me.konsolas.aac.util.TPS;
import me.konsolas.aac.util.blocks.BlockCollisionChecks;
import me.konsolas.aac.util.blocks.RelativeBlockCache;
import me.konsolas.aac.util.general.NumberUtil;
import me.konsolas.aac.util.general.PlayerItemUseReflection;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public final class Speed extends Detector implements MoveListener {
   private final BlockingQueue<MovementSnapshot> snapshotQueue = new ArrayBlockingQueue<>(1200);
   private final AsyncProcessor primaryProcessor = new AsyncProcessor(this);
   private AsyncProcessor backupProcessor = null;
   private final Map<UUID, Integer> diveTicks = new HashMap<>();
   private final Set<UUID> leftWaterPlayers = new HashSet<>();
   private final Set<UUID> exemptPlayers = new HashSet<>();
   private final ConcurrentMap<UUID, Integer> moveCounts = new ConcurrentHashMap<>();
   private final ConcurrentMap<UUID, Integer> customMoveCounts = new ConcurrentHashMap<>();
   private final Map<UUID, Integer> sneakToggleCounts = new HashMap<>();
   private final Map<UUID, Integer> useToggleCounts = new HashMap<>();
   private final Map<UUID, Integer> airTicks = new HashMap<>();
   private final Map<UUID, Double> lastDistances = new HashMap<>();
   private final Map<UUID, Integer> landingGraceTicks = new HashMap<>();
   private final ExpiringMap<UUID, Double> violationAccumulation;
   private final Map<UUID, Integer> sneakDurationTicks = new HashMap<>();
   private final Map<UUID, Integer> useDurationTicks = new HashMap<>();
   private final ExpiringMap<UUID, Location> safeSetbackLocations;
   private final Set<UUID> airViolationWarned = new HashSet<>();
   private final Set<UUID> groundedViolationTicks = new HashSet<>();
   private final Set<UUID> recentUseBypassPlayers = new HashSet<>();
   private double sprintingSpeed = 0.7;
   private double sneakingSpeed = 0.3;
   private double surfaceSpeed = 0.2;
   private double underwaterSpeed = 0.15;
   private double cobwebSpeed = 0.1;
   private double itemSpeed = 0.25;
   private double soulSandSpeed = 0.35;
   private double iceSpeed = 0.7;
   private double jumpMultiplier = 1.5;
   private boolean useMoveCounting = true;
   private int maxMoves = 22;
   private double speedMultiplier = 0.2;
   private double slowMultiplier = 0.05;
   private int maxSneakToggles = 15;
   private int maxUseToggles = 15;
   private int divePeriod = 20;
   private double diveSpeed = 0.5;
   private int vlsToBand = 6;
   private int clearVlDelay = 20;
   private double bandMultiplier = 3.0;
   private boolean enabled = true;
   private double customMaxMoves = 20.5;
   private int time = 100;
   private boolean bypassBand = false;
   private boolean useFlags = true;
   private double velMultiplier = 2.0;
   private static final double BASE_AIR_DRAG = 1.0;
   private static final double AIR_FRICTION = 0.99;

   public Speed(AAC plugin) {
      super(plugin);
      this.sprintingSpeed = this.plugin.getConfig().getDouble("speed.sprinting_speed", this.sprintingSpeed);
      this.sneakingSpeed = this.plugin.getConfig().getDouble("speed.sneaking_speed", this.sneakingSpeed);
      this.surfaceSpeed = this.plugin.getConfig().getDouble("speed.surface_speed", this.surfaceSpeed);
      this.underwaterSpeed = this.plugin.getConfig().getDouble("speed.underwater_speed", this.underwaterSpeed);
      this.cobwebSpeed = this.plugin.getConfig().getDouble("speed.cobweb_speed", this.cobwebSpeed);
      this.soulSandSpeed = this.plugin.getConfig().getDouble("speed.soul_sand_speed", this.soulSandSpeed);
      this.itemSpeed = this.plugin.getConfig().getDouble("speed.item_speed", this.itemSpeed);
      this.iceSpeed = this.plugin.getConfig().getDouble("speed.ice_speed", this.iceSpeed);
      this.divePeriod = this.plugin.getConfig().getInt("speed.dive_period", this.divePeriod);
      this.jumpMultiplier = this.plugin.getConfig().getDouble("speed.jump_multiplier", this.jumpMultiplier);
      this.useMoveCounting = this.plugin.getConfig().getBoolean("speed.use_move_counting", this.useMoveCounting);
      this.maxMoves = this.plugin.getConfig().getInt("speed.max_moves", this.maxMoves);
      this.speedMultiplier = this.plugin.getConfig().getDouble("speed.speed_multiplier", this.speedMultiplier);
      this.slowMultiplier = this.plugin.getConfig().getDouble("speed.slow_multiplier", this.slowMultiplier);
      this.maxSneakToggles = this.plugin.getConfig().getInt("speed.max_sneak_toggles", this.maxSneakToggles);
      this.maxUseToggles = this.plugin.getConfig().getInt("speed.max_use_toggles", this.maxUseToggles);
      this.diveSpeed = this.plugin.getConfig().getDouble("speed.dive_speed", this.diveSpeed);
      this.vlsToBand = this.plugin.getConfig().getInt("speed.vls_to_band", this.vlsToBand);
      this.clearVlDelay = this.plugin.getConfig().getInt("speed.clear_vl_delay", this.clearVlDelay);
      this.bandMultiplier = this.plugin.getConfig().getDouble("speed.band_multiplier", this.bandMultiplier);
      this.enabled = this.plugin.getConfig().getBoolean("speed.custom.enabled", this.enabled);
      this.customMaxMoves = this.plugin.getConfig().getDouble("speed.custom.max_moves", this.customMaxMoves);
      this.time = this.plugin.getConfig().getInt("speed.custom.time", this.time);
      this.bypassBand = this.plugin.getConfig().getBoolean("speed.custom.bypass_band", this.bypassBand);
      this.useFlags = this.plugin.getConfig().getBoolean("speed.use_flags", this.useFlags);
      this.velMultiplier = this.plugin.getConfig().getDouble("speed.vel_multiplier", this.velMultiplier);
      this.scheduledTaskIds.add(Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, new DecaySlowTask(this), 20L, 53L));
      if (this.useMoveCounting) {
         this.scheduledTaskIds.add(Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, new ResetMoveCountTask(this), 10L, 50L));
      }

      if (this.enabled) {
         this.scheduledTaskIds.add(Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, new ResetCustomMoveCountTask(this), 10L, this.time));
      }

      this.scheduledTaskIds.add(Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, new ResetToggleCountsTask(this), 6L, 20L));
      this.violationAccumulation = ExpiringMap.builder().expirationPolicy(ExpirationPolicy.ACCESSED).expiration(this.clearVlDelay, TimeUnit.SECONDS).build();
      this.safeSetbackLocations = ExpiringMap.builder().expirationPolicy(ExpirationPolicy.ACCESSED).expiration(2L, TimeUnit.SECONDS).build();
      BlockPlacePacketListener blockPlacePacketListener = new BlockPlacePacketListener(this, this.plugin, Client.BLOCK_PLACE);
      this.packetListeners.add(blockPlacePacketListener);
      ProtocolLibrary.getProtocolManager().addPacketListener(blockPlacePacketListener);
      this.primaryProcessor.start();
   }

   @Override
   public void setCurrentTick(long ticks) {
      if (ticks % 79L == 0L) {
         this.airViolationWarned.clear();
      }
   }

   public static boolean hasSolidCeiling(Player player) {
      return player.getLocation().add(0.0, 2.1, 0.5).getBlock().getType().isSolid()
         || player.getLocation().add(0.5, 2.1, 0.0).getBlock().getType().isSolid()
         || player.getLocation().add(0.0, 2.1, -0.5).getBlock().getType().isSolid()
         || player.getLocation().add(-0.5, 2.1, 0.0).getBlock().getType().isSolid()
         || player.getLocation().add(-0.5, 2.1, -0.5).getBlock().getType().isSolid()
         || player.getLocation().add(-0.5, 2.1, 0.5).getBlock().getType().isSolid()
         || player.getLocation().add(0.5, 2.1, -0.5).getBlock().getType().isSolid()
         || player.getLocation().add(0.5, 2.1, 0.5).getBlock().getType().isSolid();
   }

   public static boolean isNearLowBlock(Player player) {
      Block block = player.getLocation().getBlock();

      for (BlockFace face : BlockFace.values()) {
         Material material = block.getRelative(face).getType();
         if (BlockCollisionChecks.isSlabOrStair(material) || material == Material.TRAP_DOOR || material == Material.IRON_TRAPDOOR) {
            return true;
         }
      }

      return false;
   }

   @EventHandler
   public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
      this.recentUseBypassPlayers.add(event.getPlayer().getUniqueId());
      this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new ClearInteractBypassTask(this, event), 2L);
   }

   @EventHandler
   public void onBlockPlace(BlockPlaceEvent event) {
      this.recentUseBypassPlayers.add(event.getPlayer().getUniqueId());
      this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new ClearPlaceBypassTask(this, event), 2L);
   }

   @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
   public void onSpeedPlayerJoin(PlayerJoinEvent event) {
      this.violationLevels.put(event.getPlayer().getUniqueId(), 0);
      this.safeSetbackLocations.put(event.getPlayer().getUniqueId(), event.getPlayer().getLocation());
   }

   @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
   public void onPlayerTeleport(PlayerTeleportEvent event) {
      if (this.plugin.movementDispatcher.getTeleportExempt(event.getPlayer()) == null) {
         this.exemptPlayers.add(event.getPlayer().getUniqueId());
         Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new ClearTeleportBypassTask(this, event), 10L);
      }
   }

   @EventHandler(ignoreCancelled = true)
   public void onPlayerRespawn(PlayerRespawnEvent event) {
      this.exemptPlayers.add(event.getPlayer().getUniqueId());
      Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new ClearRespawnBypassTask(this, event), 50L);
   }

   @EventHandler
   public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
      if (!this.plugin.checkManager.isPlayerExempt(event.getPlayer())) {
         if (!this.sneakToggleCounts.containsKey(event.getPlayer().getUniqueId())) {
            this.sneakToggleCounts.put(event.getPlayer().getUniqueId(), 1);
         } else {
            this.sneakToggleCounts.put(event.getPlayer().getUniqueId(), this.sneakToggleCounts.get(event.getPlayer().getUniqueId()) + 1);
            if (this.sneakToggleCounts.get(event.getPlayer().getUniqueId()) > this.maxSneakToggles) {
               if (this.plugin.checkManager.getDetector(HackType.FLY) != null && ((Fly)this.plugin.checkManager.getDetector(HackType.FLY)).isSlimeLaunched(event.getPlayer())) {
                  return;
               }

               if (this.recordViolation(
                  event.getPlayer().getUniqueId(),
                  3,
                  HackType.SPEED,
                  event.getPlayer().getName()
                     + " failed Speed: tried to toggle sneak too quickly ("
                     + this.sneakToggleCounts.get(event.getPlayer().getUniqueId())
                     + " toggles/second)"
               )) {
                  if (this.violationAccumulation.containsKey(event.getPlayer().getUniqueId())) {
                     if (this.violationAccumulation.get(event.getPlayer().getUniqueId()) > this.vlsToBand) {
                        event.getPlayer().teleport(this.getOrUpdateSetbackLocation(event.getPlayer(), this.plugin.movementDispatcher.getSafeLocation(event.getPlayer())), TeleportCause.UNKNOWN);
                     }

                     this.violationAccumulation.put(event.getPlayer().getUniqueId(), this.violationAccumulation.get(event.getPlayer().getUniqueId()) + 1.0);
                  } else {
                     this.violationAccumulation.put(event.getPlayer().getUniqueId(), 1.3);
                  }
               }

               return;
            }
         }

         this.plugin
            .verboseLogger
            .logPlayerDebug(
               event.getPlayer(), "(Speed) PlayerToggleSneakEvent sneaks[m:" + this.maxSneakToggles + ",r:" + this.sneakToggleCounts.get(event.getPlayer().getUniqueId()) + "]"
            );
      }
   }

   @EventHandler
   public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
      if (!event.isFlying()) {
         this.exemptPlayers.add(event.getPlayer().getUniqueId());
         Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new ClearFlightBypassTask(this, event), 50L);
      }
   }

   @Override
   public boolean shouldProcessPlayerMove(PlayerMoveEvent event) {
      if (this.exemptPlayers.contains(event.getPlayer().getUniqueId())) {
         return false;
      }

      if (!event.getPlayer().isDead()
         && !event.getPlayer().isFlying()
         && !event.getPlayer().getGameMode().equals(GameMode.SPECTATOR)
         && event.getPlayer().getVehicle() == null) {
         for (Entity entity : event.getPlayer().getNearbyEntities(3.0, 3.0, 3.0)) {
            if (entity instanceof Boat) {
               return false;
            }
         }

         return (this.plugin.checkManager.getDetector(HackType.FLY) == null || !((Fly)this.plugin.checkManager.getDetector(HackType.FLY)).isSlimeLaunched(event.getPlayer()))
            && !event.getPlayer().getAllowFlight();
      } else {
         this.diveTicks.put(event.getPlayer().getUniqueId(), 20);
         return false;
      }
   }

   @Override
   public void onPlayerMove(PlayerMoveEvent event, boolean flag, boolean flag2, RelativeBlockCache relativeBlockCache, RelativeBlockCache relativeBlockCache2) {
      if (this.enabled) {
         this.customMoveCounts.putIfAbsent(event.getPlayer().getUniqueId(), 0);
         this.customMoveCounts.put(event.getPlayer().getUniqueId(), this.customMoveCounts.get(event.getPlayer().getUniqueId()) + 1);
         if (this.customMoveCounts.get(event.getPlayer().getUniqueId()).intValue() > this.customMaxMoves * (20.0 / TPS.getTps()) * (this.time / 20.0)) {
            if (this.useFlags) {
               this.plugin
                  .verboseLogger
                  .logPlayerDebug(
                     event.getPlayer(),
                     "(Speed) use_move_counting customMoveEvents[m:"
                        + this.maxMoves * (20.0 / TPS.getTps())
                        + ",r:"
                        + this.customMoveCounts.get(event.getPlayer().getUniqueId())
                        + "]"
                  );
            }

            if (this.plugin.checkManager.getDetector(HackType.FLY) != null && ((Fly)this.plugin.checkManager.getDetector(HackType.FLY)).isSlimeLaunched(event.getPlayer())) {
               return;
            }

            if (this.recordViolation(
               event.getPlayer().getUniqueId(),
               1,
               HackType.SPEED,
               event.getPlayer().getName()
                  + " failed Speed: tried to speed up time ("
                  + this.customMoveCounts.get(event.getPlayer().getUniqueId()).intValue() / (this.time / 20.0)
                  + " moves/second)"
            )) {
               if (this.plugin.checkManager.getDetector(HackType.FLY) != null && ((Fly)this.plugin.checkManager.getDetector(HackType.FLY)).isSlimeLaunched(event.getPlayer())) {
                  return;
               }

               this.recordSafeSetbackLocation(event.getPlayer(), this.plugin.movementDispatcher.getSafeLocation(event.getPlayer()));
               if (this.bypassBand) {
                  this.plugin.movementDispatcher.setTeleportExempt(this, event.getPlayer());
                  event.getPlayer().teleport(this.getOrUpdateSetbackLocation(event.getPlayer(), this.plugin.movementDispatcher.getSafeLocation(event.getPlayer())), TeleportCause.UNKNOWN);
               } else if (this.violationAccumulation.containsKey(event.getPlayer().getUniqueId())) {
                  if (this.violationAccumulation.get(event.getPlayer().getUniqueId()) > this.vlsToBand) {
                     this.plugin.movementDispatcher.setTeleportExempt(this, event.getPlayer());
                     event.getPlayer().teleport(this.getOrUpdateSetbackLocation(event.getPlayer(), this.plugin.movementDispatcher.getSafeLocation(event.getPlayer())), TeleportCause.UNKNOWN);
                  }

                  this.violationAccumulation.put(event.getPlayer().getUniqueId(), this.violationAccumulation.get(event.getPlayer().getUniqueId()) + 1.0);
               } else {
                  this.violationAccumulation.put(event.getPlayer().getUniqueId(), 1.0);
               }
            }

            return;
         }
      }

      if (this.useMoveCounting) {
         this.moveCounts.putIfAbsent(event.getPlayer().getUniqueId(), 0);
         this.moveCounts.put(event.getPlayer().getUniqueId(), this.moveCounts.get(event.getPlayer().getUniqueId()) + 1);
         if (this.moveCounts.get(event.getPlayer().getUniqueId()).intValue() > this.maxMoves * (20.0 / TPS.getTps()) * 2.5) {
            if (this.useFlags) {
               this.plugin
                  .verboseLogger
                  .logPlayerDebug(
                     event.getPlayer(),
                     "(Speed) use_move_counting moveEvents[m:" + this.maxMoves * (20.0 / TPS.getTps()) + ",r:" + this.moveCounts.get(event.getPlayer().getUniqueId()) + "]"
                  );
            }

            if (this.plugin.checkManager.getDetector(HackType.FLY) != null && ((Fly)this.plugin.checkManager.getDetector(HackType.FLY)).isSlimeLaunched(event.getPlayer())) {
               return;
            }

            if (this.recordViolation(
               event.getPlayer().getUniqueId(),
               1,
               HackType.SPEED,
               event.getPlayer().getName()
                  + " failed Speed: tried to speed up time ("
                  + this.moveCounts.get(event.getPlayer().getUniqueId()).intValue() / 2.5
                  + " moves/second)"
            )) {
               if (this.plugin.checkManager.getDetector(HackType.FLY) != null && ((Fly)this.plugin.checkManager.getDetector(HackType.FLY)).isSlimeLaunched(event.getPlayer())) {
                  return;
               }

               this.recordSafeSetbackLocation(event.getPlayer(), this.plugin.movementDispatcher.getSafeLocation(event.getPlayer()));
               if (this.violationAccumulation.containsKey(event.getPlayer().getUniqueId())) {
                  if (this.violationAccumulation.get(event.getPlayer().getUniqueId()) > this.vlsToBand) {
                     this.plugin.movementDispatcher.setTeleportExempt(this, event.getPlayer());
                     event.getPlayer().teleport(this.getOrUpdateSetbackLocation(event.getPlayer(), this.plugin.movementDispatcher.getSafeLocation(event.getPlayer())), TeleportCause.UNKNOWN);
                  }

                  this.violationAccumulation.put(event.getPlayer().getUniqueId(), this.violationAccumulation.get(event.getPlayer().getUniqueId()) + 1.0);
               } else {
                  this.violationAccumulation.put(event.getPlayer().getUniqueId(), 1.0);
               }
            }

            return;
         }
      }

      int value = -1;
      int value2 = -1;
      if (event.getPlayer().hasPotionEffect(PotionEffectType.SPEED) || event.getPlayer().hasPotionEffect(PotionEffectType.SLOW)) {
         for (PotionEffect potionEffect : event.getPlayer().getActivePotionEffects()) {
            if (potionEffect.getType().equals(PotionEffectType.SPEED)) {
               value = potionEffect.getAmplifier();
            } else if (potionEffect.getType().equals(PotionEffectType.SLOW)) {
               value2 = potionEffect.getAmplifier();
            }
         }
      }

      int value3 = value;
      int value4 = value2;
      boolean flag3 = event.getPlayer().getLocation().getBlock().getType() == Material.SOUL_SAND
         || event.getPlayer().getLocation().subtract(0.0, 1.0, 0.0).getBlock().getType() == Material.SOUL_SAND;
      boolean flag4 = Jesus.isInOrAboveLiquid(event.getPlayer()) || event.getPlayer().getLocation().getBlock().isLiquid();
      boolean flag5 = isNearLowBlock(event.getPlayer());
      boolean flag6 = this.isSubmerged(event.getPlayer());
      Block block = event.getPlayer().getLocation().getBlock();
      boolean flag7 = hasSolidCeiling(event.getPlayer());
      boolean flag8 = this.isInWeb(event.getPlayer());
      boolean flag9 = isStandingOnIce(event.getPlayer().getLocation());
      boolean flag10 = BlockCollisionChecks.isAboveBounceBlock(relativeBlockCache);
      boolean flag11 = BlockCollisionChecks.isAdjacentToSolidBlock(relativeBlockCache);

      try {
         if (this.snapshotQueue.size() > 1024) {
            this.plugin.getLogger().warning("Speed AsyncProcessor may not be emptying the queue quickly enough");
            this.plugin.getLogger().warning("Queue size is " + this.snapshotQueue.size());
            if (this.backupProcessor == null) {
               this.plugin.getLogger().severe("(Speed) Attempting to recover by starting new consumer.");
               this.backupProcessor = new AsyncProcessor(this);
               this.backupProcessor.start();
            } else {
               this.plugin.getLogger().severe("(Speed) No more consumers avaliable.");
            }

            this.plugin.getLogger().warning("Resetting queue...");
            this.snapshotQueue.clear();
         }

         this.snapshotQueue
            .add(
               new MovementSnapshot(
                  this,
                  event,
                  flag,
                  flag2,
                  value3,
                  value4,
                  event.getPlayer().getVelocity(),
                  flag3,
                  flag4,
                  flag5,
                  flag6,
                  block,
                  flag7,
                  flag8,
                  flag9,
                  this.plugin.movementDispatcher.getSafeLocation(event.getPlayer()),
                  flag10,
                  flag11
               )
            );
      } catch (IllegalStateException ex) {
         this.plugin.getLogger().severe("(Speed) Failed to queue movement. Fatal Speed error.");
         this.plugin.getLogger().severe("(Speed) No more consumers avaliable");
         this.plugin.getLogger().severe("(Speed) Speed check has been disabled.");
         this.plugin.checkManager.disableCheck(HackType.SPEED, false);
      }
   }

   @EventHandler(priority = EventPriority.MONITOR)
   public void onVehicleEnter(VehicleEnterEvent event) {
      if (event.getEntered().getType() == EntityType.PLAYER) {
         this.diveTicks.put(event.getEntered().getUniqueId(), 2);
      }
   }

   @EventHandler(priority = EventPriority.MONITOR)
   public void onVehicleExit(VehicleExitEvent event) {
      if (event.getExited().getType() == EntityType.PLAYER) {
         this.diveTicks.put(event.getExited().getUniqueId(), 2);
      }
   }

   @EventHandler
   public void onWorldUnload(WorldUnloadEvent event) {
      HashSet<UUID> set = new HashSet<>();

      for (Entry<UUID, Location> entry : this.safeSetbackLocations.entrySet()) {
         if (((Location)entry.getValue()).getWorld().equals(event.getWorld())) {
            set.add(entry.getKey());
         }
      }

      for (UUID uuid : set) {
         this.safeSetbackLocations.remove(uuid);
      }
   }

   private boolean isInWeb(Player player) {
      return player.getLocation().getBlock().getType().equals(Material.WEB) || player.getLocation().add(0.0, 1.0, 0.0).getBlock().getType().equals(Material.WEB);
   }

   private boolean isSubmerged(Player player) {
      return player.getLocation().add(0.0, 2.0, 0.0).getBlock().isLiquid();
   }

   public void clearCaches() {
      this.violationAccumulation.clear();
      this.safeSetbackLocations.clear();
      this.snapshotQueue.clear();
   }

   @Override
   public void destroy() {
      super.destroy();
      this.primaryProcessor.shutdown();
   }

   private void handleSpeedViolation(Player player, double d, double d2, Location location) {
      if (this.violationAccumulation.containsKey(player.getUniqueId())) {
         if (d > d2 * 5.0) {
            this.violationAccumulation.put(player.getUniqueId(), 10000.0);
         }

         this.violationAccumulation.put(player.getUniqueId(), this.violationAccumulation.get(player.getUniqueId()) + (d - d2) * this.bandMultiplier + 2.0);
      } else {
         if (d > d2 * 5.0) {
            this.violationAccumulation.put(player.getUniqueId(), 10000.0);
         }

         this.violationAccumulation.put(player.getUniqueId(), (d - d2) * this.bandMultiplier + 2.0);
      }

      Location location2 = this.getOrUpdateSetbackLocation(player, location);
      if (this.violationAccumulation.get(player.getUniqueId()) > this.vlsToBand) {
         Speed speed = this;
         this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new DeferredSetbackTask(this, player, location, speed, location2));
      }
   }

   public static boolean isStandingOnIce(Location location) {
      RelativeBlockCache relativeBlockCache = new RelativeBlockCache(location);
      return isIceBlock(relativeBlockCache.getSubtractedBlock(0.0, 0.6, 0.3))
         || isIceBlock(relativeBlockCache.getSubtractedBlock(0.3, 0.6, 0.0))
         || isIceBlock(relativeBlockCache.getSubtractedBlock(0.0, 0.6, -0.3))
         || isIceBlock(relativeBlockCache.getSubtractedBlock(-0.3, 0.6, 0.0))
         || isIceBlock(relativeBlockCache.getSubtractedBlock(-0.3, 0.6, -0.3))
         || isIceBlock(relativeBlockCache.getSubtractedBlock(-0.3, 0.6, 0.3))
         || isIceBlock(relativeBlockCache.getSubtractedBlock(0.3, 0.6, -0.3))
         || isIceBlock(relativeBlockCache.getSubtractedBlock(0.3, 0.6, 0.3))
         || isIceBlock(relativeBlockCache.getSubtractedBlock(0.0, 1.5, 0.3))
         || isIceBlock(relativeBlockCache.getSubtractedBlock(0.3, 1.5, 0.0))
         || isIceBlock(relativeBlockCache.getSubtractedBlock(0.0, 1.5, -0.3))
         || isIceBlock(relativeBlockCache.getSubtractedBlock(-0.3, 1.5, 0.0))
         || isIceBlock(relativeBlockCache.getSubtractedBlock(-0.3, 1.5, -0.3))
         || isIceBlock(relativeBlockCache.getSubtractedBlock(-0.3, 1.5, 0.3))
         || isIceBlock(relativeBlockCache.getSubtractedBlock(0.3, 1.5, -0.3))
         || isIceBlock(relativeBlockCache.getSubtractedBlock(0.3, 1.5, 0.3));
   }

   private static boolean isIceBlock(Block block) {
      return block.getType() == Material.ICE || block.getType() == Material.PACKED_ICE;
   }

   public Location getOrUpdateSetbackLocation(Player player, Location location) {
      this.recordSafeSetbackLocation(player, location);
      return this.safeSetbackLocations.get(player.getUniqueId());
   }

   private void recordSafeSetbackLocation(Player player, Location location) {
      if (!this.safeSetbackLocations.containsKey(player.getUniqueId())) {
         double x = Math.abs(player.getLocation().getX() - location.getX());
         double z = Math.abs(player.getLocation().getZ() - location.getZ());
         double d = Math.sqrt(Math.pow(x, 2.0) + Math.pow(z, 2.0));
         if (d < 7.0 && player.getLocation().getY() >= location.getY()) {
            this.safeSetbackLocations.put(player.getUniqueId(), location);
         } else {
            this.safeSetbackLocations.put(player.getUniqueId(), player.getLocation());
         }

         this.plugin.verboseLogger.logPlayerDebug(player, "(Speed) Updated setback to " + this.safeSetbackLocations.get(player.getUniqueId()).toVector().toString());
      }
   }


   static ConcurrentMap getMoveCounts(Speed speed) {
      return speed.moveCounts;
   }

   static ConcurrentMap getCustomMoveCounts(Speed speed) {
      return speed.customMoveCounts;
   }

   static Map getSneakToggleCounts(Speed speed) {
      return speed.sneakToggleCounts;
   }

   static Map getUseToggleCounts(Speed speed) {
      return speed.useToggleCounts;
   }


   static int getMaxUseToggles(Speed speed) {
      return speed.maxUseToggles;
   }


   static void recordSafeSetbackLocation(Speed speed, Player player, Location location) {
      speed.recordSafeSetbackLocation(player, location);
   }

   static void handleSpeedViolation(Speed speed, Player player, double d, double d2, Location location) {
      speed.handleSpeedViolation(player, d, d2, location);
   }

   static Set getRecentUseBypassPlayers(Speed speed) {
      return speed.recentUseBypassPlayers;
   }

   static Set getExemptPlayers(Speed speed) {
      return speed.exemptPlayers;
   }

   static BlockingQueue getSnapshotQueue(Speed speed) {
      return speed.snapshotQueue;
   }


   static double getSprintingSpeed(Speed speed) {
      return speed.sprintingSpeed;
   }

   static Set getGroundedViolationTicks(Speed speed) {
      return speed.groundedViolationTicks;
   }


   static ExpiringMap getViolationAccumulation(Speed speed) {
      return speed.violationAccumulation;
   }

   static int getVlsToBand(Speed speed) {
      return speed.vlsToBand;
   }


   static ExpiringMap getSafeSetbackLocations(Speed speed) {
      return speed.safeSetbackLocations;
   }


   static Map getLandingGraceTicks(Speed speed) {
      return speed.landingGraceTicks;
   }

   static boolean isUseFlags(Speed speed) {
      return speed.useFlags;
   }

   static Map getSneakDurationTicks(Speed speed) {
      return speed.sneakDurationTicks;
   }

   static double getSneakingSpeed(Speed speed) {
      return speed.sneakingSpeed;
   }

   static Map getUseDurationTicks(Speed speed) {
      return speed.useDurationTicks;
   }

   static double getItemSpeed(Speed speed) {
      return speed.itemSpeed;
   }

   static double getIceSpeed(Speed speed) {
      return speed.iceSpeed;
   }

   static double getSoulSandSpeed(Speed speed) {
      return speed.soulSandSpeed;
   }

   static double getSurfaceSpeed(Speed speed) {
      return speed.surfaceSpeed;
   }

   static Map getDiveTicks(Speed speed) {
      return speed.diveTicks;
   }

   static double getUnderwaterSpeed(Speed speed) {
      return speed.underwaterSpeed;
   }

   static int getDivePeriod(Speed speed) {
      return speed.divePeriod;
   }


   static Set getLeftWaterPlayers(Speed speed) {
      return speed.leftWaterPlayers;
   }

   static double getDiveSpeed(Speed speed) {
      return speed.diveSpeed;
   }


   static double getCobwebSpeed(Speed speed) {
      return speed.cobwebSpeed;
   }

   static double getSpeedMultiplier(Speed speed) {
      return speed.speedMultiplier;
   }

   static double getSlowMultiplier(Speed speed) {
      return speed.slowMultiplier;
   }

   static double getJumpMultiplier(Speed speed) {
      return speed.jumpMultiplier;
   }


   static Map getAirTicks(Speed speed) {
      return speed.airTicks;
   }


   static Map getLastDistances(Speed speed) {
      return speed.lastDistances;
   }


   static double getVelMultiplier(Speed speed) {
      return speed.velMultiplier;
   }


   static Set getAirViolationWarned(Speed speed) {
      return speed.airViolationWarned;
   }


   private static class DecaySlowTask implements Runnable {
      final Speed speed;

      DecaySlowTask(Speed speed) {
         this.speed = speed;
      }

      @Override
      public void run() {
         for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            this.speed.decreaseViolationLevel(player.getUniqueId(), 1);
         }
      }
   }

   private static class ClearFlightBypassTask implements Runnable {
      final PlayerToggleFlightEvent event;
      final Speed speed;

      ClearFlightBypassTask(Speed speed, PlayerToggleFlightEvent event) {
         this.speed = speed;
         this.event = event;
      }

      @Override
      public void run() {
         Speed.getExemptPlayers(this.speed).remove(this.event.getPlayer().getUniqueId());
      }
   }

   private static class DeferredSetbackTask implements Runnable {
      final Player player;
      final Location targetLocation;
      final Speed detector;
      final Location setbackLocation;
      final Speed speed;

      DeferredSetbackTask(Speed speed, Player player, Location location, Speed speed2, Location location2) {
         this.speed = speed;
         this.player = player;
         this.targetLocation = location;
         this.detector = speed2;
         this.setbackLocation = location2;
      }

      @Override
      public void run() {
         if (this.player.isOnline() && !this.player.isDead() && !this.player.isFlying() && this.speed.getOrUpdateSetbackLocation(this.player, this.targetLocation) != null) {
            this.speed.getPlugin().movementDispatcher.setTeleportExempt(this.detector, this.player);
            this.player.teleport(this.setbackLocation, TeleportCause.UNKNOWN);
         }
      }
   }

   class DamageCauseSwitch {
      static final int[] SWITCH_TABLE = new int[DamageCause.values().length];

      static {
         try {
            SWITCH_TABLE[DamageCause.ENTITY_ATTACK.ordinal()] = 1;
         } catch (NoSuchFieldError ex) {
         }

         try {
            SWITCH_TABLE[DamageCause.PROJECTILE.ordinal()] = 2;
         } catch (NoSuchFieldError ex2) {
         }

         try {
            SWITCH_TABLE[DamageCause.ENTITY_EXPLOSION.ordinal()] = 3;
         } catch (NoSuchFieldError ex3) {
         }
      }
   }

   private static class ResetMoveCountTask implements Runnable {
      final Speed speed;

      ResetMoveCountTask(Speed speed) {
         this.speed = speed;
      }

      @Override
      public void run() {
         for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            Speed.getMoveCounts(this.speed).put(player.getUniqueId(), 0);
         }
      }
   }

   private static class ResetCustomMoveCountTask implements Runnable {
      final Speed speed;

      ResetCustomMoveCountTask(Speed speed) {
         this.speed = speed;
      }

      @Override
      public void run() {
         for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            Speed.getCustomMoveCounts(this.speed).put(player.getUniqueId(), 0);
         }
      }
   }

   private static class ResetToggleCountsTask implements Runnable {
      final Speed speed;

      ResetToggleCountsTask(Speed speed) {
         this.speed = speed;
      }

      @Override
      public void run() {
         for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            Speed.getSneakToggleCounts(this.speed).put(player.getUniqueId(), 0);
            Speed.getUseToggleCounts(this.speed).put(player.getUniqueId(), 0);
            this.speed.decreaseViolationLevel(player.getUniqueId(), 1);
         }
      }
   }

   private static class BlockPlacePacketListener extends PacketAdapter {
      final Speed speed;

      BlockPlacePacketListener(Speed speed, Plugin plugin, PacketType... packetTypeArray) {
         super(plugin, packetTypeArray);
         this.speed = speed;
      }

      public void onPacketReceiving(PacketEvent event) {
         if (event.getPacketType() == Client.BLOCK_PLACE) {
            if (((AAC)this.plugin).checkManager.isPlayerExempt(event.getPlayer())) {
               return;
            }

            if (!Speed.getUseToggleCounts(this.speed).containsKey(event.getPlayer().getUniqueId())) {
               Speed.getUseToggleCounts(this.speed).put(event.getPlayer().getUniqueId(), 1);
            } else {
               Speed.getUseToggleCounts(this.speed).put(event.getPlayer().getUniqueId(), (Integer)Speed.getUseToggleCounts(this.speed).get(event.getPlayer().getUniqueId()) + 1);
               if ((Integer)Speed.getUseToggleCounts(this.speed).get(event.getPlayer().getUniqueId()) > Speed.getMaxUseToggles(this.speed)) {
                  if (((AAC)this.plugin).checkManager.getDetector(HackType.FLY) != null
                     && ((Fly)((AAC)this.plugin).checkManager.getDetector(HackType.FLY)).isSlimeLaunched(event.getPlayer())) {
                     return;
                  }

                  if (this.speed.recordViolation(event.getPlayer().getUniqueId(), 2, HackType.SPEED, event.getPlayer().getName()
                        + " failed Speed: tried to toggle use too quickly ("
                        + Speed.getUseToggleCounts(this.speed).get(event.getPlayer().getUniqueId())
                        + " toggles/second)")) {
                     Speed.recordSafeSetbackLocation(this.speed, event.getPlayer(), ((AAC)this.plugin).movementDispatcher.getSafeLocation(event.getPlayer()));
                     Speed.handleSpeedViolation(this.speed, event.getPlayer(), 2.0, 1.0, ((AAC)this.plugin).movementDispatcher.getSafeLocation(event.getPlayer()));
                  }
               }
            }
         }
      }
   }

   private static class ClearInteractBypassTask implements Runnable {
      final PlayerInteractEntityEvent event;
      final Speed speed;

      ClearInteractBypassTask(Speed speed, PlayerInteractEntityEvent event) {
         this.speed = speed;
         this.event = event;
      }

      @Override
      public void run() {
         Speed.getRecentUseBypassPlayers(this.speed).remove(this.event.getPlayer().getUniqueId());
      }
   }

   private static class ClearPlaceBypassTask implements Runnable {
      final BlockPlaceEvent event;
      final Speed speed;

      ClearPlaceBypassTask(Speed speed, BlockPlaceEvent event) {
         this.speed = speed;
         this.event = event;
      }

      @Override
      public void run() {
         Speed.getRecentUseBypassPlayers(this.speed).remove(this.event.getPlayer().getUniqueId());
      }
   }

   private static class ClearTeleportBypassTask implements Runnable {
      final PlayerTeleportEvent event;
      final Speed speed;

      ClearTeleportBypassTask(Speed speed, PlayerTeleportEvent event) {
         this.speed = speed;
         this.event = event;
      }

      @Override
      public void run() {
         Speed.getExemptPlayers(this.speed).remove(this.event.getPlayer().getUniqueId());
      }
   }

   private static class ClearRespawnBypassTask implements Runnable {
      final PlayerRespawnEvent event;
      final Speed speed;

      ClearRespawnBypassTask(Speed speed, PlayerRespawnEvent event) {
         this.speed = speed;
         this.event = event;
      }

      @Override
      public void run() {
         Speed.getExemptPlayers(this.speed).remove(this.event.getPlayer().getUniqueId());
      }
   }

   private static class WebSetbackTask implements Runnable {
      final PlayerMoveEvent event;
      final AsyncProcessor processor;

      WebSetbackTask(AsyncProcessor asyncProcessor, PlayerMoveEvent event) {
         this.processor = asyncProcessor;
         this.event = event;
      }

      @Override
      public void run() {
         this.event.getPlayer().teleport((Location)Speed.getSafeSetbackLocations(this.processor.speed).get(this.event.getPlayer().getUniqueId()), TeleportCause.UNKNOWN);
      }
   }

   private static class AsyncProcessor extends Thread {
      final Speed speed;

      private AsyncProcessor(Speed speed) {
         this.speed = speed;
      }

      @Override
      public void run() {
         try {
            while (true) {
               MovementSnapshot movementSnapshot = (MovementSnapshot)Speed.getSnapshotQueue(this.speed).take();
               PlayerMoveEvent event = movementSnapshot.event;
               Player player = event.getPlayer();
               double x = Math.sqrt(Math.pow(movementSnapshot.velocity.getX(), 2.0) + Math.pow(movementSnapshot.velocity.getZ(), 2.0));
               double x2 = Math.sqrt(Math.pow(event.getTo().getX() - event.getFrom().getX(), 2.0) + Math.pow(event.getTo().getZ() - event.getFrom().getZ(), 2.0));
               boolean flag = this.speed.getPlugin().combatLogoutListener.hasRecentDamage(player);
               StringBuilder sb = new StringBuilder();
               double d = Speed.getSprintingSpeed(this.speed);
               boolean flag2 = false;
               boolean flag3 = false;
               if (movementSnapshot.fromOnGround && movementSnapshot.toOnGround) {
                  if (Speed.getGroundedViolationTicks(this.speed).contains(event.getPlayer().getUniqueId())) {
                     sb.append("grounded ");
                     flag2 = true;
                  } else {
                     Speed.getGroundedViolationTicks(this.speed).add(event.getPlayer().getUniqueId());
                  }
               } else {
                  Speed.getGroundedViolationTicks(this.speed).remove(event.getPlayer().getUniqueId());
               }

               if (!NumberUtil.isWithinRange(event.getTo().getY() - event.getFrom().getY(), -0.04, 0.062)
                  && movementSnapshot.blockAtFeet != null
                  && movementSnapshot.blockAtFeet.getType() == Material.WEB
                  && player.getLocation().getY() % 1.0 != 0.0
                  && this.speed.recordViolation(event.getPlayer().getUniqueId(), 1, HackType.SPEED, event.getPlayer().getName() + " failed Speed: moved wrongly in a web (" + (event.getTo().getY() - event.getFrom().getY()) + ")")) {
                  if (Speed.getViolationAccumulation(this.speed).containsKey(event.getPlayer().getUniqueId())) {
                     if ((Double)Speed.getViolationAccumulation(this.speed).get(event.getPlayer().getUniqueId()) > Speed.getVlsToBand(this.speed)) {
                        this.speed.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(this.speed.getPlugin(), new WebSetbackTask(this, event));
                     }

                     Speed.getViolationAccumulation(this.speed).put(event.getPlayer().getUniqueId(), (Double)Speed.getViolationAccumulation(this.speed).get(event.getPlayer().getUniqueId()) + 1.0);
                  } else {
                     Speed.getViolationAccumulation(this.speed).put(event.getPlayer().getUniqueId(), 1.3);
                  }
               }

               if (!NumberUtil.isWithinRange(event.getFrom().getX(), event.getTo().getX(), 0.04) || !NumberUtil.isWithinRange(event.getFrom().getZ(), event.getTo().getZ(), 0.04)) {
                  if (Speed.getLandingGraceTicks(this.speed).containsKey(player.getUniqueId())) {
                     Speed.getLandingGraceTicks(this.speed).put(player.getUniqueId(), (Integer)Speed.getLandingGraceTicks(this.speed).get(player.getUniqueId()) - 1);
                     if ((Integer)Speed.getLandingGraceTicks(this.speed).get(player.getUniqueId()) <= 0) {
                        Speed.getLandingGraceTicks(this.speed).remove(player.getUniqueId());
                     }

                     d *= 1.5;
                     d += 0.3;
                  }

                  if (player.isSprinting() && Speed.isUseFlags(this.speed)) {
                     sb.append("sprinting");
                  }

                  if (player.isSneaking()) {
                     if (Speed.getSneakDurationTicks(this.speed).containsKey(player.getUniqueId())) {
                        Speed.getSneakDurationTicks(this.speed).put(player.getUniqueId(), (Integer)Speed.getSneakDurationTicks(this.speed).get(player.getUniqueId()) + 1);
                     } else {
                        Speed.getSneakDurationTicks(this.speed).put(player.getUniqueId(), 1);
                     }

                     if ((Integer)Speed.getSneakDurationTicks(this.speed).get(player.getUniqueId()) > 7) {
                        d = Speed.getSneakingSpeed(this.speed);
                        if (Speed.isUseFlags(this.speed)) {
                           sb.append("sneaking ");
                        }

                        flag3 = true;
                     }
                  } else {
                     Speed.getSneakDurationTicks(this.speed).remove(player.getUniqueId());
                  }

                  if ((!PlayerItemUseReflection.isUsingItem(player) || Speed.getRecentUseBypassPlayers(this.speed).contains(player.getUniqueId())) && !player.isBlocking()) {
                     Speed.getUseDurationTicks(this.speed).remove(player.getUniqueId());
                  } else {
                     if (Speed.getUseDurationTicks(this.speed).containsKey(player.getUniqueId())) {
                        Speed.getUseDurationTicks(this.speed).put(player.getUniqueId(), (Integer)Speed.getUseDurationTicks(this.speed).get(player.getUniqueId()) + 1);
                     } else {
                        Speed.getUseDurationTicks(this.speed).put(player.getUniqueId(), 1);
                     }

                     if ((Integer)Speed.getUseDurationTicks(this.speed).get(player.getUniqueId()) > 7) {
                        d = Speed.getItemSpeed(this.speed);
                        if (Speed.isUseFlags(this.speed)) {
                           sb.append("item ");
                        }

                        flag3 = true;
                     }
                  }

                  if (movementSnapshot.onIce) {
                     d = Speed.getIceSpeed(this.speed);
                     if (Speed.isUseFlags(this.speed)) {
                        sb.append("ice ");
                     }
                  }

                  if (movementSnapshot.onSoulSand) {
                     d = Speed.getSoulSandSpeed(this.speed);
                     if (Speed.isUseFlags(this.speed)) {
                        sb.append("soulsand ");
                     }
                  }

                  boolean flag4 = movementSnapshot.inLiquid;
                  boolean flag5 = false;
                  if (flag4 && !movementSnapshot.nearLowBlock && !flag) {
                     if (Speed.isUseFlags(this.speed)) {
                        sb.append("watersurface ");
                     }

                     if (player.getInventory().getBoots() != null && player.getInventory().getBoots().getEnchantmentLevel(Enchantment.DEPTH_STRIDER) > 0) {
                        flag5 = true;
                        switch (player.getInventory().getBoots().getEnchantmentLevel(Enchantment.DEPTH_STRIDER)) {
                           case 1:
                              d *= 0.5;
                              if (Speed.isUseFlags(this.speed)) {
                                 sb.append("depthstrider1 ");
                              }
                              break;
                           case 2:
                              d *= 0.8;
                              if (Speed.isUseFlags(this.speed)) {
                                 sb.append("depthstrider2 ");
                              }
                              break;
                           default:
                              if (Speed.isUseFlags(this.speed)) {
                                 sb.append("depthstrider3 ");
                              }
                        }
                     } else {
                        d = Speed.getSurfaceSpeed(this.speed);
                     }
                  } else if (Speed.getDiveTicks(this.speed).containsKey(player.getUniqueId())) {
                     Speed.getDiveTicks(this.speed).remove(player.getUniqueId());
                  }

                  if (movementSnapshot.submerged && !flag5 && !flag) {
                     if (Speed.isUseFlags(this.speed)) {
                        sb.append("underwater ");
                     }

                     if (player.getInventory().getBoots() != null && player.getInventory().getBoots().getEnchantmentLevel(Enchantment.DEPTH_STRIDER) > 0) {
                        switch (player.getInventory().getBoots().getEnchantmentLevel(Enchantment.DEPTH_STRIDER)) {
                           case 1:
                              d *= 0.6;
                              if (Speed.isUseFlags(this.speed)) {
                                 sb.append("depthstrider1 ");
                              }
                              break;
                           case 2:
                              d *= 0.8;
                              if (Speed.isUseFlags(this.speed)) {
                                 sb.append("depthstrider2 ");
                              }
                              break;
                           default:
                              if (Speed.isUseFlags(this.speed)) {
                                 sb.append("depthstrider3 ");
                              }
                        }
                     } else {
                        d = Speed.getUnderwaterSpeed(this.speed);
                     }
                  }

                  if (Speed.getDiveTicks(this.speed).containsKey(event.getPlayer().getUniqueId())) {
                     if ((Integer)Speed.getDiveTicks(this.speed).get(event.getPlayer().getUniqueId()) >= Speed.getDivePeriod(this.speed)) {
                        this.speed.getPlugin().verboseLogger.logPlayerDebug(event.getPlayer(), "(Speed) Finished dive");
                        Speed.getDiveTicks(this.speed).remove(event.getPlayer().getUniqueId());
                        Speed.getLeftWaterPlayers(this.speed).add(event.getPlayer().getUniqueId());
                     } else {
                        Speed.getDiveTicks(this.speed).put(event.getPlayer().getUniqueId(), (Integer)Speed.getDiveTicks(this.speed).get(event.getPlayer().getUniqueId()) + 1);
                        d = Speed.getDiveSpeed(this.speed);
                        if (Speed.isUseFlags(this.speed)) {
                           sb.append("diving ");
                        }
                     }
                  } else {
                     if (flag4 && !Speed.getLeftWaterPlayers(this.speed).contains(player.getUniqueId())) {
                        Speed.getDiveTicks(this.speed).put(player.getUniqueId(), 0);
                        this.speed.getPlugin().verboseLogger.logPlayerDebug(player, "(Speed) Started dive");
                        d = Speed.getDiveSpeed(this.speed);
                        if (Speed.isUseFlags(this.speed)) {
                           sb.append("diving ");
                        }
                     }

                     if (!flag4 && !movementSnapshot.submerged && Speed.getLeftWaterPlayers(this.speed).contains(player.getUniqueId())) {
                        Speed.getLeftWaterPlayers(this.speed).remove(player.getUniqueId());
                        if (Speed.isUseFlags(this.speed)) {
                           this.speed.getPlugin().verboseLogger.logPlayerDebug(player, "(Speed) Left the water");
                        }
                     }
                  }

                  if (movementSnapshot.inWeb) {
                     d = Speed.getCobwebSpeed(this.speed);
                     if (Speed.isUseFlags(this.speed)) {
                        sb.append("cobweb ");
                     }
                  }

                  if (movementSnapshot.speedAmplifier >= 0) {
                     d += d * (movementSnapshot.speedAmplifier + 1) * Speed.getSpeedMultiplier(this.speed);
                     if (Speed.isUseFlags(this.speed)) {
                        sb.append("speed").append(movementSnapshot.speedAmplifier + 1).append(" ");
                     }
                  }

                  if (movementSnapshot.slowAmplifier >= 0) {
                     d -= d * (movementSnapshot.slowAmplifier + 1) * Speed.getSlowMultiplier(this.speed);
                     if (Speed.isUseFlags(this.speed)) {
                        sb.append("slow").append(movementSnapshot.slowAmplifier + 1).append(" ");
                     }
                  }

                  d = d * 5.0 * player.getWalkSpeed();
                  if (BlockCollisionChecks.isFenceOrGate(movementSnapshot.blockAtFeet.getType())
                     || movementSnapshot.hasCeiling
                     || movementSnapshot.nearLowBlock
                     || movementSnapshot.blockAtFeet.getType() == Material.SNOW
                     || movementSnapshot.blockAtFeet.getType() == Material.VINE
                     || flag
                     || movementSnapshot.aboveCollidable) {
                     if (Speed.isUseFlags(this.speed)) {
                        sb.append("boost ");
                     }

                     d *= Speed.getJumpMultiplier(this.speed);
                  }

                  if (movementSnapshot.toOnGround
                     && movementSnapshot.fromOnGround
                     && !this.speed.getPlugin().playerState.hasActiveVelocity(player)
                     && this.speed.getPlugin().playerState.getLastVelocityPacketTime(player) + 200L < System.currentTimeMillis()) {
                     if (flag && d < Speed.getSprintingSpeed(this.speed)) {
                        d = Speed.getSprintingSpeed(this.speed);
                     }

                     Speed.getAirTicks(this.speed).remove(event.getPlayer().getUniqueId());
                     String str = sb.toString();
                     if (Speed.isUseFlags(this.speed)) {
                        this.speed.getPlugin().verboseLogger.logPlayerDebug(player, "(Speed) PlayerMoveEvent distance[r:" + x2 + ",t:" + d + ",f:" + str + "]");
                     }

                     int value = flag2 ? 2 : 1;
                     if (flag3
                        && !flag2
                        && (!Speed.getViolationAccumulation(this.speed).containsKey(event.getPlayer().getUniqueId()) || (Double)Speed.getViolationAccumulation(this.speed).get(event.getPlayer().getUniqueId()) < 2.0)) {
                        d += 0.1;
                     }

                     if (x2 > d) {
                        if (x2 - d > 5.0) {
                           if (this.speed.recordViolation(player.getUniqueId(), (int)(x2 - d - 4.0) * 10 > 20 ? 20 : (int)(x2 - d - 4.0) * 10, HackType.SPEED, player.getName() + " failed Speed: tried to teleport??? (" + x2 + " blocks, expected " + d + "), f:" + str)
                              && (
                                 this.speed.getPlugin().checkManager.getDetector(HackType.FLY) == null
                                    || !((Fly)this.speed.getPlugin().checkManager.getDetector(HackType.FLY)).isSlimeLaunched(event.getPlayer())
                              )) {
                              Speed.handleSpeedViolation(this.speed, player, x2, d, movementSnapshot.fromLocation);
                           }
                        } else if (this.speed.recordViolation(player.getUniqueId(), value, HackType.SPEED, player.getName() + " failed Speed: tried to move too quickly (" + x2 + " blocks, expected " + d + "), f:" + str)
                           && (
                              this.speed.getPlugin().checkManager.getDetector(HackType.FLY) == null
                                 || !((Fly)this.speed.getPlugin().checkManager.getDetector(HackType.FLY)).isSlimeLaunched(event.getPlayer())
                           )) {
                           if (flag3) {
                              Speed.handleSpeedViolation(this.speed, player, x2 + 1.0, d, movementSnapshot.fromLocation);
                           } else {
                              Speed.handleSpeedViolation(this.speed, player, x2, d, movementSnapshot.fromLocation);
                           }
                        }
                     }
                  } else {
                     if (flag3) {
                        d *= Speed.getJumpMultiplier(this.speed);
                     }

                     if (x < 0.05) {
                        x = 0.05;
                     }

                     sb.append("hvel").append(x).append(' ');
                     Double doubleVal = (Double)Speed.getLastDistances(this.speed).get(player.getUniqueId());
                     Speed.getLastDistances(this.speed).put(player.getUniqueId(), x2);
                     
                     boolean flag6 = true;
                     double d2 = this.speed.getPlugin().playerState.getLastHorizontalVelocity(player);
                     long ticks = this.speed.getPlugin().playerState.getLastVelocityPacketTime(player);
                     long ticks2;
                     if (ticks != Long.MIN_VALUE) {
                        ticks2 = System.currentTimeMillis() - ticks;
                     } else {
                        ticks2 = Long.MAX_VALUE;
                     }

                     boolean flag7 = ticks2 < 200L || this.speed.getPlugin().playerState.hasActiveVelocity(event.getPlayer());
                     if (movementSnapshot.fromOnGround && !flag7 && ticks2 > 100L) {
                        sb.append("takeoff ");
                        double d3 = d;
                        d3 += d3 * (Speed.getSpeedMultiplier(this.speed) * (movementSnapshot.speedAmplifier + 1));
                        d3 -= d3 * (Speed.getSlowMultiplier(this.speed) * (movementSnapshot.slowAmplifier + 1));
                        d3 += player.getWalkSpeed();
                        d = d3;
                        if (x2 <= d) {
                           flag6 = false;
                        }
                     } else if (movementSnapshot.toOnGround && !flag7 && ticks2 > 100L) {
                        sb.append("landing ");
                        d *= 1.2;
                        if (x2 <= d) {
                           flag6 = false;
                        }

                        Speed.getLandingGraceTicks(this.speed).put(player.getUniqueId(), 2);
                     } else if (flag7 && !this.speed.getPlugin().combatLogoutListener.hasRecentDamage(player)) {
                        sb.append("velocity ");
                        flag6 = false;
                        d = x * Speed.getVelMultiplier(this.speed) > d2 ? x * Speed.getVelMultiplier(this.speed) : d2;
                        if (x2 <= d) {
                           flag6 = false;
                        }
                     } else if (this.speed.getPlugin().combatLogoutListener.hasRecentDamage(player) || flag7) {
                        sb.append("damaged ");
                        flag6 = false;
                        DamageCause damageCause = this.speed.getPlugin().combatLogoutListener.getLastDamageCause(player);
                        switch (DamageCauseSwitch.SWITCH_TABLE[damageCause.ordinal()]) {
                           case 1:
                           case 2:
                              sb.append("attacked");
                              d = Speed.getSprintingSpeed(this.speed) * Speed.getJumpMultiplier(this.speed) + d2 * Speed.getVelMultiplier(this.speed);
                              break;
                           case 3:
                              sb.append("blownup");
                              d = Speed.getSprintingSpeed(this.speed) * Speed.getJumpMultiplier(this.speed) + x * 1.5 * Speed.getVelMultiplier(this.speed);
                              break;
                           default:
                              d = Speed.getSprintingSpeed(this.speed) * Speed.getJumpMultiplier(this.speed) * 1.0;
                        }
                     }

                     if (!flag7 && !this.speed.getPlugin().combatLogoutListener.hasRecentDamage(event.getPlayer()) && movementSnapshot.underCollidable) {
                        d = Speed.getSprintingSpeed(this.speed) * Speed.getJumpMultiplier(this.speed);
                     }

                     if (flag6) {
                        if (Speed.getAirTicks(this.speed).containsKey(player.getUniqueId())) {
                           Speed.getAirTicks(this.speed).put(player.getUniqueId(), (Integer)Speed.getAirTicks(this.speed).get(player.getUniqueId()) + 1);
                        } else {
                           Speed.getAirTicks(this.speed).put(player.getUniqueId(), 0);
                        }

                        int value2 = (Integer)Speed.getAirTicks(this.speed).get(event.getPlayer().getUniqueId());
                        sb.append("phase").append(value2).append(' ');
                        double d4 = NumberUtil.pow(1.0, 0.99, (Integer)Speed.getAirTicks(this.speed).get(event.getPlayer().getUniqueId()));
                        d4 += d4 * (Speed.getSpeedMultiplier(this.speed) * (movementSnapshot.speedAmplifier + 1));
                        d4 -= d4 * (Speed.getSlowMultiplier(this.speed) * (movementSnapshot.slowAmplifier + 1));
                        d4 += player.getWalkSpeed();
                        d *= d4;
                     }

                     String str2 = sb.toString();
                     if (Speed.isUseFlags(this.speed)) {
                        this.speed.getPlugin().verboseLogger.logPlayerDebug(player, "(Speed) AirPlayerMoveEvent distance[r:" + x2 + ",t:" + d + ",f:" + str2 + "]");
                     }

                     if (x2 > d) {
                        if (!Speed.getAirViolationWarned(this.speed).contains(event.getPlayer().getUniqueId()) && !(x2 - d > 0.8)) {
                           Speed.getAirViolationWarned(this.speed).add(event.getPlayer().getUniqueId());
                        } else if (this.speed.recordViolation(player.getUniqueId(), 1, HackType.SPEED, player.getName() + " failed Speed: moved too quickly midair (" + x2 + " blocks, expected " + d + "), f:" + str2)) {
                           Speed.handleSpeedViolation(this.speed, player, x2, d, movementSnapshot.fromLocation);
                        }
                     }
                  }
               }
            }
         } catch (InterruptedException ex) {
            this.speed.getPlugin().getLogger().info("Speed AsyncProcessor is exiting due to an interrupt");
         } catch (Exception ex2) {
            this.speed.getPlugin().getLogger().warning("(Speed) (AsyncProcessor) WARNING: Automatically handled possible error");
            ex2.printStackTrace();
            Speed.getSnapshotQueue(this.speed).clear();
            this.run();
         }
      }

      public void shutdown() {
         this.interrupt();
      }

   }

   private static class MovementSnapshot {
      PlayerMoveEvent event;
      boolean fromOnGround;
      boolean toOnGround;
      int speedAmplifier;
      int slowAmplifier;
      Vector velocity;
      boolean onSoulSand;
      boolean inLiquid;
      boolean nearLowBlock;
      boolean submerged;
      Block blockAtFeet;
      boolean hasCeiling;
      boolean inWeb;
      boolean onIce;
      Location fromLocation;
      boolean underCollidable;
      boolean aboveCollidable;
      final Speed speed;

      MovementSnapshot(
         Speed speed,
         PlayerMoveEvent event,
         boolean flag,
         boolean flag2,
         int i,
         int i2,
         Vector vector,
         boolean flag3,
         boolean flag4,
         boolean flag5,
         boolean flag6,
         Block block,
         boolean flag7,
         boolean flag8,
         boolean flag9,
         Location location,
         boolean flag10,
         boolean flag11
      ) {
         this.speed = speed;
         this.event = event;
         this.fromOnGround = flag;
         this.toOnGround = flag2;
         this.speedAmplifier = i;
         this.slowAmplifier = i2;
         this.velocity = vector;
         this.onSoulSand = flag3;
         this.inLiquid = flag4;
         this.nearLowBlock = flag5;
         this.submerged = flag6;
         this.blockAtFeet = block;
         this.hasCeiling = flag7;
         this.inWeb = flag8;
         this.onIce = flag9;
         this.fromLocation = location;
         this.underCollidable = flag10;
         this.aboveCollidable = flag11;
      }
   }
}
