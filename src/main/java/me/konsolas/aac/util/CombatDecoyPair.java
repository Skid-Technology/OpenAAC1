package me.konsolas.aac.util;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.wrappers.EnumWrappers.EntityUseAction;
import com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import me.konsolas.aac.AAC;
import me.konsolas.aac.detectors.combat.Angle;
import me.konsolas.aac.internal.packetwrapper.WrapperPlayClientUseEntity;
import me.konsolas.aac.internal.packetwrapper.WrapperPlayServerEntityDestroy;
import me.konsolas.aac.internal.packetwrapper.WrapperPlayServerEntityTeleport;
import me.konsolas.aac.internal.packetwrapper.WrapperPlayServerNamedEntitySpawn;
import me.konsolas.aac.internal.packetwrapper.WrapperPlayServerPlayerInfo;
import me.konsolas.aac.internal.packetwrapper.WrapperPlayServerSpawnEntityLiving;
import me.konsolas.aac.util.general.NumberUtil;
import me.konsolas.aac.util.general.RandomPlayerNameGenerator;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class CombatDecoyPair {
   private static int ticksSpawned = 5;
   private static double maxDiff = 0.1;
   private static double maxReach = 5.0;
   private static int percentDistance = 80;
   private static double minDist = 2.7;
   private static int listenDelay = 2;
   PlayerIdentity leftIdentity;
   PlayerIdentity rightIdentity;
   boolean completed = false;
   private UUID playerId;
   private Entity targetEntity;
   private AAC plugin;
   private HitListener hitListener;
   private PacketListener packetListener;
   private int playerHits = 0;
   private int decoyHits = 0;
   private BukkitTask tickTask;
   private int registerTaskId;
   private int elapsedTicks;
   private int leftEntityId;
   private int rightEntityId;

   public CombatDecoyPair(AAC plugin, Player player, Entity entity, HitListener hitListener) {
      this.hitListener = hitListener;
      this.plugin = plugin;
      this.playerId = player.getUniqueId();
      this.targetEntity = entity;
      this.packetListener = new AttackPacketListener(this, plugin, WrapperPlayClientUseEntity.TYPE);
   }

   public static void loadConfig(AAC plugin) {
      ticksSpawned = plugin.getConfig().getInt("killaura.npc.ticks_spawned", ticksSpawned);
      maxDiff = plugin.getConfig().getDouble("killaura.npc.max_diff", maxDiff);
      maxReach = plugin.getConfig().getDouble("killaura.npc.max_reach", maxReach);
      percentDistance = plugin.getConfig().getInt("killaura.npc.percent_distance", percentDistance);
      minDist = plugin.getConfig().getDouble("killaura.npc.min_dist", minDist);
      listenDelay = plugin.getConfig().getInt("killaura.npc.listen_delay", listenDelay);
   }

   public void spawn(EntityType entityType) {
      if (entityType == EntityType.PLAYER) {
         this.spawnPlayerDecoys();
      } else {
         this.spawnEntityDecoys(entityType);
      }
   }

   private void spawnPlayerDecoys() {
      if (this.getPlayer() != null) {
         this.leftIdentity = PlayerIdentityPool.getInstance().generateRandomIdentity();
         Vector vector = this.computeSideOffset(this.getPlayer().getLocation().toVector(), this.getTargetEntity().getLocation().toVector(), percentDistance, Side.LEFT);
         vector.setY(vector.getY() + 0.15);
         WrapperPlayServerNamedEntitySpawn wrapperPlayServerNamedEntitySpawn = this.createNamedSpawnPacket(vector, true, this.leftIdentity);
         WrapperPlayServerPlayerInfo wrapperPlayServerPlayerInfo = this.createPlayerInfoPacket(PlayerInfoAction.ADD_PLAYER, false, this.leftIdentity);
         wrapperPlayServerPlayerInfo.sendPacket(this.getPlayer());
         wrapperPlayServerNamedEntitySpawn.sendPacket(this.getPlayer());
         this.leftEntityId = wrapperPlayServerNamedEntitySpawn.getEntityID();
         WrapperPlayServerPlayerInfo wrapperPlayServerPlayerInfo2 = this.createPlayerInfoPacket(PlayerInfoAction.REMOVE_PLAYER, false, this.leftIdentity);
         wrapperPlayServerPlayerInfo2.sendPacket(this.getPlayer());
         this.rightIdentity = PlayerIdentityPool.getInstance().generateRandomIdentity();
         Vector vector2 = this.computeSideOffset(this.getPlayer().getLocation().toVector(), this.getTargetEntity().getLocation().toVector(), percentDistance, Side.RIGHT);
         vector2.setY(vector2.getY() + 0.15);
         WrapperPlayServerNamedEntitySpawn wrapperPlayServerNamedEntitySpawn2 = this.createNamedSpawnPacket(vector2, true, this.rightIdentity);
         WrapperPlayServerPlayerInfo wrapperPlayServerPlayerInfo3 = this.createPlayerInfoPacket(PlayerInfoAction.ADD_PLAYER, false, this.rightIdentity);
         wrapperPlayServerPlayerInfo3.sendPacket(this.getPlayer());
         wrapperPlayServerNamedEntitySpawn2.sendPacket(this.getPlayer());
         this.rightEntityId = wrapperPlayServerNamedEntitySpawn2.getEntityID();
         WrapperPlayServerPlayerInfo wrapperPlayServerPlayerInfo4 = this.createPlayerInfoPacket(PlayerInfoAction.REMOVE_PLAYER, false, this.rightIdentity);
         wrapperPlayServerPlayerInfo4.sendPacket(this.getPlayer());
         this.registerTaskId = Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new RegisterFirstListener(this), listenDelay);
         this.scheduleNextTick();
      }
   }

   private void spawnEntityDecoys(EntityType entityType) {
      if (this.getPlayer() != null) {
         Vector vector = this.computeSideOffset(this.getPlayer().getLocation().toVector(), this.getTargetEntity().getLocation().toVector(), percentDistance, Side.LEFT);
         vector.setY(vector.getY() + 0.15);
         WrapperPlayServerSpawnEntityLiving wrapperPlayServerSpawnEntityLiving = new WrapperPlayServerSpawnEntityLiving();
         wrapperPlayServerSpawnEntityLiving.setType(entityType);
         wrapperPlayServerSpawnEntityLiving.setEntityID(this.allocateFakeEntityId(this.getPlayer()));
         wrapperPlayServerSpawnEntityLiving.setX(vector.getX());
         wrapperPlayServerSpawnEntityLiving.setY(vector.getY());
         wrapperPlayServerSpawnEntityLiving.setZ(vector.getZ());
         WrappedDataWatcher wrappedDataWatcher = new WrappedDataWatcher();
         wrappedDataWatcher.setObject(6, AAC.random.nextFloat() * 15.0F);
         byte b = 32;
         wrappedDataWatcher.setObject(0, b);
         wrapperPlayServerSpawnEntityLiving.setMetadata(wrappedDataWatcher);
         this.leftEntityId = wrapperPlayServerSpawnEntityLiving.getEntityID();
         wrapperPlayServerSpawnEntityLiving.sendPacket(this.getPlayer());
         Vector vector2 = this.computeSideOffset(this.getPlayer().getLocation().toVector(), this.getTargetEntity().getLocation().toVector(), percentDistance, Side.RIGHT);
         vector2.setY(vector2.getY() + 0.15);
         WrapperPlayServerSpawnEntityLiving wrapperPlayServerSpawnEntityLiving2 = new WrapperPlayServerSpawnEntityLiving();
         wrapperPlayServerSpawnEntityLiving2.setType(entityType);
         wrapperPlayServerSpawnEntityLiving2.setEntityID(this.allocateFakeEntityId(this.getPlayer()));
         wrapperPlayServerSpawnEntityLiving2.setX(vector2.getX());
         wrapperPlayServerSpawnEntityLiving2.setY(vector2.getY());
         wrapperPlayServerSpawnEntityLiving2.setZ(vector2.getZ());
         WrappedDataWatcher wrappedDataWatcher2 = new WrappedDataWatcher();
         wrappedDataWatcher2.setObject(6, AAC.random.nextFloat() * 15.0F);
         byte b2 = 32;
         wrappedDataWatcher2.setObject(0, b2);
         wrapperPlayServerSpawnEntityLiving2.setMetadata(wrappedDataWatcher2);
         this.rightEntityId = wrapperPlayServerSpawnEntityLiving2.getEntityID();
         wrapperPlayServerSpawnEntityLiving2.sendPacket(this.getPlayer());
         this.registerTaskId = Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new RegisterSecondListener(this), listenDelay);
         this.scheduleNextTick();
      }
   }

   private void updateDecoyPositions() {
      if (this.getPlayer() != null && this.getTargetEntity() != null) {
         try {
            if (this.getPlayer().getLocation().distance(this.getTargetEntity().getLocation()) < minDist) {
               this.destroy();
            }
         } catch (IllegalArgumentException ex) {
            this.destroy();
         }

         WrapperPlayServerEntityTeleport wrapperPlayServerEntityTeleport = new WrapperPlayServerEntityTeleport();
         wrapperPlayServerEntityTeleport.setPitch(AAC.random.nextFloat() * 80.0F);
         wrapperPlayServerEntityTeleport.setYaw(AAC.random.nextFloat() * 359.0F);
         Vector vector = this.computeSideOffset(this.getPlayer().getLocation().toVector(), this.getTargetEntity().getLocation().toVector(), percentDistance, Side.LEFT);
         wrapperPlayServerEntityTeleport.setX(vector.getX());
         wrapperPlayServerEntityTeleport.setY(vector.getY() + 0.1);
         wrapperPlayServerEntityTeleport.setZ(vector.getZ());
         wrapperPlayServerEntityTeleport.setEntityID(this.leftEntityId);
         wrapperPlayServerEntityTeleport.sendPacket(this.getPlayer());
         WrapperPlayServerEntityTeleport wrapperPlayServerEntityTeleport2 = new WrapperPlayServerEntityTeleport();
         wrapperPlayServerEntityTeleport2.setPitch(AAC.random.nextFloat() * 80.0F);
         wrapperPlayServerEntityTeleport2.setYaw(AAC.random.nextFloat() * 359.0F);
         Vector vector2 = this.computeSideOffset(this.getPlayer().getLocation().toVector(), this.getTargetEntity().getLocation().toVector(), percentDistance, Side.RIGHT);
         wrapperPlayServerEntityTeleport2.setX(vector2.getX());
         wrapperPlayServerEntityTeleport2.setY(vector2.getY() + 0.1);
         wrapperPlayServerEntityTeleport2.setZ(vector2.getZ());
         wrapperPlayServerEntityTeleport2.setEntityID(this.rightEntityId);
         wrapperPlayServerEntityTeleport2.sendPacket(this.getPlayer());
      }
   }

   private void scheduleNextTick() {
      this.tickTask = Bukkit.getScheduler().runTaskLaterAsynchronously(this.plugin, new TickTask(this), 1L);
   }

   private Vector computeSideOffset(Vector vector, Vector vector2, double d, Side side) {
      Vector vector3 = vector2.clone().subtract(vector);
      double d2 = vector3.length();
      vector3 = vector3.normalize().multiply(d2 * (d / 100.0));
      Vector vector4 = vector.clone().add(vector3);
      Vector vector5 = vector.clone().subtract(vector2);
      Vector vector6 = vector5.clone();
      if (side == Side.LEFT) {
         vector5.setX(-vector6.getZ());
         vector5.setZ(vector6.getX());
      } else {
         vector5.setZ(-vector6.getX());
         vector5.setX(vector6.getZ());
      }

      vector5.normalize();
      return vector4.add(vector5.multiply(0.3));
   }

   public void destroy() {
      if (this.getPlayer() == null) {
         this.tickTask.cancel();
         Bukkit.getServer().getScheduler().cancelTask(this.registerTaskId);
         ProtocolLibrary.getProtocolManager().removePacketListener(this.packetListener);
         this.notifyListener();
      } else {
         this.createDestroyPacket(this.leftEntityId).sendPacket(this.getPlayer());
         this.createDestroyPacket(this.rightEntityId).sendPacket(this.getPlayer());
         this.tickTask.cancel();
         Bukkit.getServer().getScheduler().cancelTask(this.registerTaskId);
         ProtocolLibrary.getProtocolManager().removePacketListener(this.packetListener);
         this.notifyListener();
      }
   }

   private void notifyListener() {
      if (!this.completed) {
         this.hitListener.onDecoyComplete(this.playerHits, this.decoyHits);
         this.completed = true;
      }
   }

   private WrapperPlayServerNamedEntitySpawn createNamedSpawnPacket(Vector vector, boolean flag, PlayerIdentity playerIdentity) {
      int value = this.allocateFakeEntityId(this.getPlayer());
      WrapperPlayServerNamedEntitySpawn wrapperPlayServerNamedEntitySpawn = new WrapperPlayServerNamedEntitySpawn();
      wrapperPlayServerNamedEntitySpawn.setEntityID(value);
      wrapperPlayServerNamedEntitySpawn.setPosition(vector);
      wrapperPlayServerNamedEntitySpawn.setPlayerUUID(playerIdentity.getUniqueId());
      wrapperPlayServerNamedEntitySpawn.setYaw((float)(AAC.random.nextInt() % 360));
      wrapperPlayServerNamedEntitySpawn.setPitch(AAC.random.nextInt() % 100 - 50.0F);
      WrappedDataWatcher wrappedDataWatcher = new WrappedDataWatcher();
      byte b;
      if (flag) {
         byte b2 = 32;
         b = Byte.valueOf(b2);
      } else {
         b = 0;
      }

      byte b3 = b;
      wrappedDataWatcher.setObject(0, b3);
      wrappedDataWatcher.setObject(6, ThreadLocalRandom.current().nextInt(19) + 1.0F);
      wrapperPlayServerNamedEntitySpawn.setMetadata(wrappedDataWatcher);
      return wrapperPlayServerNamedEntitySpawn;
   }

   private WrapperPlayServerPlayerInfo createPlayerInfoPacket(PlayerInfoAction playerInfoAction, boolean flag, PlayerIdentity playerIdentity) {
      WrapperPlayServerPlayerInfo wrapperPlayServerPlayerInfo = new WrapperPlayServerPlayerInfo();
      wrapperPlayServerPlayerInfo.setAction(playerInfoAction);
      WrappedGameProfile wrappedGameProfile = new WrappedGameProfile(playerIdentity.getUniqueId(), flag ? playerIdentity.getName() : RandomPlayerNameGenerator.generateName());
      PlayerInfoData playerInfoData = new PlayerInfoData(wrappedGameProfile, 1, NativeGameMode.SURVIVAL, WrappedChatComponent.fromText(flag ? playerIdentity.getName() : RandomPlayerNameGenerator.generateName()));
      ArrayList list = new ArrayList();
      list.add(playerInfoData);
      wrapperPlayServerPlayerInfo.setData(list);
      return wrapperPlayServerPlayerInfo;
   }

   private WrapperPlayServerEntityDestroy createDestroyPacket(int i) {
      WrapperPlayServerEntityDestroy wrapperPlayServerEntityDestroy = new WrapperPlayServerEntityDestroy();
      wrapperPlayServerEntityDestroy.setEntityIds(new int[]{i});
      return wrapperPlayServerEntityDestroy;
   }

   private int allocateFakeEntityId(Player player) {
      return player.getEntityId() + 1073741823 + ThreadLocalRandom.current().nextInt(100000);
   }

   private Player getPlayer() {
      return Bukkit.getPlayer(this.playerId);
   }

   private Entity getTargetEntity() {
      return this.targetEntity;
   }

   static Player getPlayer(CombatDecoyPair combatDecoyPair) {
      return combatDecoyPair.getPlayer();
   }

   static int getLeftEntityId(CombatDecoyPair combatDecoyPair) {
      return combatDecoyPair.leftEntityId;
   }

   static int getRightEntityId(CombatDecoyPair combatDecoyPair) {
      return combatDecoyPair.rightEntityId;
   }

   static int incrementPlayerHits(CombatDecoyPair combatDecoyPair) {
      return combatDecoyPair.playerHits++;
   }

   static Entity getTargetEntity(CombatDecoyPair combatDecoyPair) {
      return combatDecoyPair.getTargetEntity();
   }

   static double getMaxDiff() {
      return maxDiff;
   }

   static double getMaxReach() {
      return maxReach;
   }

   static int incrementDecoyHits(CombatDecoyPair combatDecoyPair) {
      return combatDecoyPair.decoyHits++;
   }

   static PacketListener getPacketListener(CombatDecoyPair combatDecoyPair) {
      return combatDecoyPair.packetListener;
   }

   static void updateDecoyPositions(CombatDecoyPair combatDecoyPair) {
      combatDecoyPair.updateDecoyPositions();
   }

   static int incrementElapsedTicks(CombatDecoyPair combatDecoyPair) {
      return combatDecoyPair.elapsedTicks++;
   }

   static int getElapsedTicks(CombatDecoyPair combatDecoyPair) {
      return combatDecoyPair.elapsedTicks;
   }

   static int getTicksSpawned() {
      return ticksSpawned;
   }

   static void scheduleNextTick(CombatDecoyPair combatDecoyPair) {
      combatDecoyPair.scheduleNextTick();
   }

   private static class AttackPacketListener extends PacketAdapter {
      final CombatDecoyPair decoyPair;

      AttackPacketListener(CombatDecoyPair combatDecoyPair, Plugin plugin, PacketType... packetTypeArray) {
         super(plugin, packetTypeArray);
         this.decoyPair = combatDecoyPair;
      }

      public void onPacketReceiving(PacketEvent event) {
         try {
            if (event.getPacketType() == WrapperPlayClientUseEntity.TYPE) {
               WrapperPlayClientUseEntity wrapperPlayClientUseEntity = new WrapperPlayClientUseEntity(event.getPacket());
               if (wrapperPlayClientUseEntity.getType() == EntityUseAction.ATTACK && event.getPlayer().getUniqueId().equals(CombatDecoyPair.getPlayer(this.decoyPair).getUniqueId())) {
                  int value = wrapperPlayClientUseEntity.getTargetID();
                  if (value == CombatDecoyPair.getLeftEntityId(this.decoyPair) || value == CombatDecoyPair.getRightEntityId(this.decoyPair)) {
                     CombatDecoyPair.incrementPlayerHits(this.decoyPair);
                     double d = Angle.calculateLookAlignment(CombatDecoyPair.getPlayer(this.decoyPair).getLocation(), CombatDecoyPair.getTargetEntity(this.decoyPair).getLocation());

                     try {
                        if (NumberUtil.isWithinRange(d, 1.0, CombatDecoyPair.getMaxDiff())
                           && CombatDecoyPair.getPlayer(this.decoyPair).getLocation().add(0.0, 1.0, 0.0).distance(CombatDecoyPair.getTargetEntity(this.decoyPair).getLocation()) < CombatDecoyPair.getMaxReach()) {
                           wrapperPlayClientUseEntity.setTargetID(CombatDecoyPair.getTargetEntity(this.decoyPair).getEntityId());
                        } else {
                           event.setCancelled(true);
                        }
                     } catch (IllegalArgumentException ex) {
                        this.decoyPair.destroy();
                     }
                  } else if (value == CombatDecoyPair.getTargetEntity(this.decoyPair).getEntityId()) {
                     CombatDecoyPair.incrementDecoyHits(this.decoyPair);
                     event.setCancelled(true);
                  }
               }
            }
         } catch (NullPointerException ex2) {
            this.decoyPair.destroy();
         }
      }
   }

   public interface HitListener {
      void onDecoyComplete(int i, int i2);
   }

   private static class RegisterFirstListener implements Runnable {
      final CombatDecoyPair decoyPair;

      RegisterFirstListener(CombatDecoyPair combatDecoyPair) {
         this.decoyPair = combatDecoyPair;
      }

      @Override
      public void run() {
         ProtocolLibrary.getProtocolManager().addPacketListener(CombatDecoyPair.getPacketListener(this.decoyPair));
      }
   }

   private static class RegisterSecondListener implements Runnable {
      final CombatDecoyPair decoyPair;

      RegisterSecondListener(CombatDecoyPair combatDecoyPair) {
         this.decoyPair = combatDecoyPair;
      }

      @Override
      public void run() {
         ProtocolLibrary.getProtocolManager().addPacketListener(CombatDecoyPair.getPacketListener(this.decoyPair));
      }
   }

   private enum Side {
      LEFT,
      RIGHT;

      private static final Side[] VALUES = new Side[]{LEFT, RIGHT};
   }

   private static class TickTask implements Runnable {
      final CombatDecoyPair decoyPair;

      TickTask(CombatDecoyPair combatDecoyPair) {
         this.decoyPair = combatDecoyPair;
      }

      @Override
      public void run() {
         CombatDecoyPair.updateDecoyPositions(this.decoyPair);
         CombatDecoyPair.incrementElapsedTicks(this.decoyPair);
         if (CombatDecoyPair.getElapsedTicks(this.decoyPair) < CombatDecoyPair.getTicksSpawned()) {
            CombatDecoyPair.scheduleNextTick(this.decoyPair);
         } else {
            this.decoyPair.destroy();
         }
      }
   }
}
