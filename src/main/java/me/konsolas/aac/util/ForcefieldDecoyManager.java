package me.konsolas.aac.util;

import com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import me.konsolas.aac.AAC;
import me.konsolas.aac.detectors.combat.Forcefield;
import me.konsolas.aac.internal.packetwrapper.WrapperPlayServerEntityDestroy;
import me.konsolas.aac.internal.packetwrapper.WrapperPlayServerEntityEquipment;
import me.konsolas.aac.internal.packetwrapper.WrapperPlayServerEntityMetadata;
import me.konsolas.aac.internal.packetwrapper.WrapperPlayServerEntityStatus;
import me.konsolas.aac.internal.packetwrapper.WrapperPlayServerEntityTeleport;
import me.konsolas.aac.internal.packetwrapper.WrapperPlayServerNamedEntitySpawn;
import me.konsolas.aac.internal.packetwrapper.WrapperPlayServerPlayerInfo;
import me.konsolas.aac.internal.packetwrapper.WrapperPlayServerRelEntityMove;
import me.konsolas.aac.util.general.RandomPlayerNameGenerator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class ForcefieldDecoyManager {
   private static boolean headPlayerInvisible = false;
   private static boolean useSidePlayers = true;
   private static boolean pastPlayerNames = true;
   private static boolean headTablist = true;
   private static double headPlayerHeight = 4.0;
   private static boolean noHeadLookingUp = true;
   private static boolean onlinePlayerNames = true;
   private static boolean invisibleOutsidePvp = true;
   private static boolean randomSprint = true;
   private static boolean equipArmour = true;
   private static boolean equipHeldItem = true;
   private static boolean fakeOnGround = true;
   private static boolean sendDamagedAnimation = true;
   private final AAC plugin;
   private final Forcefield forcefield;
   private int tickPeriod = 3;
   private int maxTicks = 600;
   private BukkitTask tickTask;
   private int secondSpawnTaskId;
   private PlayerIdentity headIdentity;
   private PlayerIdentity rearIdentity;
   private PlayerIdentity sideIdentity;
   private int unusedEntityId;
   private Map<Integer, Boolean> decoyVisibilityFlags = new HashMap<>();
   private UUID playerId;
   boolean unusedFlag = false;
   boolean currentlyInvisible = false;
   private int headEntityId;
   private int rearEntityId;
   private int sideEntityId;
   private int elapsedTicks;
   private boolean useOnlineName;

   public ForcefieldDecoyManager(AAC plugin, Player player, Forcefield forcefield) {
      this.plugin = plugin;
      this.playerId = player.getUniqueId();
      this.forcefield = forcefield;
      this.useOnlineName = onlinePlayerNames;
   }

   public static void loadConfig(AAC plugin) {
      headPlayerInvisible = plugin.getConfig().getBoolean("forcefield.head_player_invisible");
      pastPlayerNames = plugin.getConfig().getBoolean("forcefield.past_player_names");
      useSidePlayers = plugin.getConfig().getBoolean("forcefield.use_side_players");
      headPlayerHeight = plugin.getConfig().getDouble("forcefield.head_player_height");
      noHeadLookingUp = plugin.getConfig().getBoolean("forcefield.no_head_player_looking_up");
      headTablist = plugin.getConfig().getBoolean("forcefield.head_tablist");
      onlinePlayerNames = plugin.getConfig().getBoolean("forcefield.online_player_names");
      invisibleOutsidePvp = plugin.getConfig().getBoolean("forcefield.invisible_outside_pvp");
      randomSprint = plugin.getConfig().getBoolean("forcefield.magic.random_sprint");
      equipArmour = plugin.getConfig().getBoolean("forcefield.magic.equip_armour");
      equipHeldItem = plugin.getConfig().getBoolean("forcefield.magic.equip_held_item");
      fakeOnGround = plugin.getConfig().getBoolean("forcefield.magic.fake_on_ground");
      sendDamagedAnimation = plugin.getConfig().getBoolean("forcefield.magic.send_damaged_animation");
   }

   public boolean isSideEntityId(int i) {
      return !useSidePlayers ? false : i == this.rearEntityId || i == this.sideEntityId;
   }

   public boolean isHeadEntityId(int i) {
      return i == this.headEntityId;
   }

   private Player getPlayer() {
      return Bukkit.getPlayer(this.playerId);
   }

   public void spawn() {
      Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new SpawnTask(this), AAC.random.nextInt(2));
   }

   private void scheduleTick() {
      this.tickTask = Bukkit.getScheduler().runTaskLaterAsynchronously(this.plugin, new TickTask(this), this.tickPeriod);
   }

   private void tickPositions() {
      WrapperPlayServerEntityTeleport wrapperPlayServerEntityTeleport = new WrapperPlayServerEntityTeleport();
      if (this.getPlayer() == null) {
         this.forcefield.removeDecoyManager(this.playerId);
      } else {
         if ((!(this.getPlayer().getLocation().getPitch() < -30.0F) || !noHeadLookingUp) && AAC.random.nextInt(100) <= 85) {
            wrapperPlayServerEntityTeleport.setPitch(AAC.random.nextFloat() * 80.0F);
            wrapperPlayServerEntityTeleport.setYaw(AAC.random.nextFloat() * 359.0F);
            wrapperPlayServerEntityTeleport.setX(this.getPlayer().getLocation().getX() + AAC.random.nextFloat());
            wrapperPlayServerEntityTeleport.setY(this.getPlayer().getLocation().getY() + headPlayerHeight);
            wrapperPlayServerEntityTeleport.setZ(this.getPlayer().getLocation().getZ() + AAC.random.nextFloat());
            wrapperPlayServerEntityTeleport.setEntityID(this.headEntityId);
            wrapperPlayServerEntityTeleport.sendPacket(this.getPlayer());
         } else {
            double yaw = (int)this.getPlayer().getLocation().getYaw() + 270;
            double d = Math.toRadians(yaw);
            wrapperPlayServerEntityTeleport.setPitch(AAC.random.nextFloat() * 80.0F);
            wrapperPlayServerEntityTeleport.setYaw(AAC.random.nextFloat() * 359.0F);
            Vector vector = this.getPlayer().getLocation().add(3.4 * Math.cos(d), 0.0, 3.4 * Math.sin(d)).toVector();
            wrapperPlayServerEntityTeleport.setX(vector.getX());
            wrapperPlayServerEntityTeleport.setY(vector.getY());
            wrapperPlayServerEntityTeleport.setZ(vector.getZ());
            wrapperPlayServerEntityTeleport.setEntityID(this.headEntityId);
            wrapperPlayServerEntityTeleport.sendPacket(this.getPlayer());
         }

         if ((this.plugin.combatLogoutListener.isInCombat(this.getPlayer()) || !invisibleOutsidePvp) && !headPlayerInvisible) {
            if (this.currentlyInvisible) {
               this.currentlyInvisible = false;
               WrapperPlayServerEntityMetadata wrapperPlayServerEntityMetadata = createMetadataPacket(this.headEntityId, false, AAC.random.nextBoolean());
               wrapperPlayServerEntityMetadata.sendPacket(this.getPlayer());
            } else if (AAC.random.nextInt(100) > 90) {
               WrapperPlayServerEntityMetadata wrapperPlayServerEntityMetadata2 = createMetadataPacket(this.headEntityId, false, AAC.random.nextBoolean());
               wrapperPlayServerEntityMetadata2.sendPacket(this.getPlayer());
            }
         } else if (!this.currentlyInvisible) {
            this.currentlyInvisible = true;
            WrapperPlayServerEntityMetadata wrapperPlayServerEntityMetadata3 = createMetadataPacket(this.headEntityId, true, AAC.random.nextBoolean());
            wrapperPlayServerEntityMetadata3.sendPacket(this.getPlayer());
         } else if (AAC.random.nextInt(100) > 90) {
            WrapperPlayServerEntityMetadata wrapperPlayServerEntityMetadata4 = createMetadataPacket(this.headEntityId, true, AAC.random.nextBoolean());
            wrapperPlayServerEntityMetadata4.sendPacket(this.getPlayer());
         }

         if (useSidePlayers) {
            WrapperPlayServerEntityTeleport wrapperPlayServerEntityTeleport2 = new WrapperPlayServerEntityTeleport();
            double yaw2 = (int)this.getPlayer().getLocation().getYaw() + 190;
            double d2 = Math.toRadians(yaw2);
            wrapperPlayServerEntityTeleport2.setPitch(AAC.random.nextInt(100) - 50);
            wrapperPlayServerEntityTeleport2.setYaw(AAC.random.nextFloat() * 359.0F);
            Vector vector2 = this.getPlayer().getLocation().add(1.7 * Math.cos(d2), AAC.random.nextFloat() / 2.0F - 1.0F, 1.7 * Math.sin(d2)).toVector();
            wrapperPlayServerEntityTeleport2.setX(vector2.getX());
            wrapperPlayServerEntityTeleport2.setY(vector2.getY());
            wrapperPlayServerEntityTeleport2.setZ(vector2.getZ());
            wrapperPlayServerEntityTeleport2.setEntityID(this.rearEntityId);
            wrapperPlayServerEntityTeleport2.sendPacket(this.getPlayer());
            WrapperPlayServerEntityTeleport wrapperPlayServerEntityTeleport3 = new WrapperPlayServerEntityTeleport();
            yaw2 = (int)this.getPlayer().getLocation().getYaw() + 10;
            d2 = Math.toRadians(yaw2);
            wrapperPlayServerEntityTeleport3.setPitch(AAC.random.nextInt(100) - 50);
            wrapperPlayServerEntityTeleport3.setYaw(AAC.random.nextFloat() * 359.0F);
            vector2 = this.getPlayer().getLocation().add(1.7 * Math.cos(d2), AAC.random.nextFloat() / 2.0F - 1.0F, 1.7 * Math.sin(d2)).toVector();
            wrapperPlayServerEntityTeleport3.setX(vector2.getX());
            wrapperPlayServerEntityTeleport3.setY(vector2.getY());
            wrapperPlayServerEntityTeleport3.setZ(vector2.getZ());
            wrapperPlayServerEntityTeleport3.setEntityID(this.sideEntityId);
            wrapperPlayServerEntityTeleport3.sendPacket(this.getPlayer());
         }

         if ((!this.plugin.combatLogoutListener.isInCombat(this.getPlayer()) && invisibleOutsidePvp || headPlayerInvisible) && this.unusedFlag) {
            WrapperPlayServerEntityEquipment wrapperPlayServerEntityEquipment = new WrapperPlayServerEntityEquipment();
            WrapperPlayServerEntityEquipment wrapperPlayServerEntityEquipment2 = new WrapperPlayServerEntityEquipment();
            WrapperPlayServerEntityEquipment wrapperPlayServerEntityEquipment3 = new WrapperPlayServerEntityEquipment();
            WrapperPlayServerEntityEquipment wrapperPlayServerEntityEquipment4 = new WrapperPlayServerEntityEquipment();
            WrapperPlayServerEntityEquipment wrapperPlayServerEntityEquipment5 = new WrapperPlayServerEntityEquipment();
            wrapperPlayServerEntityEquipment.setEntityID(this.headEntityId);
            wrapperPlayServerEntityEquipment2.setEntityID(this.headEntityId);
            wrapperPlayServerEntityEquipment3.setEntityID(this.headEntityId);
            wrapperPlayServerEntityEquipment4.setEntityID(this.headEntityId);
            wrapperPlayServerEntityEquipment5.setEntityID(this.headEntityId);
            wrapperPlayServerEntityEquipment.setSlot(0);
            wrapperPlayServerEntityEquipment2.setSlot(1);
            wrapperPlayServerEntityEquipment3.setSlot(2);
            wrapperPlayServerEntityEquipment4.setSlot(3);
            wrapperPlayServerEntityEquipment5.setSlot(4);
            wrapperPlayServerEntityEquipment.setItem(new ItemStack(Material.AIR));
            wrapperPlayServerEntityEquipment2.setItem(new ItemStack(Material.AIR));
            wrapperPlayServerEntityEquipment3.setItem(new ItemStack(Material.AIR));
            wrapperPlayServerEntityEquipment4.setItem(new ItemStack(Material.AIR));
            wrapperPlayServerEntityEquipment5.setItem(new ItemStack(Material.AIR));
            if (equipHeldItem) {
               wrapperPlayServerEntityEquipment.sendPacket(this.getPlayer());
            }

            if (equipArmour) {
               wrapperPlayServerEntityEquipment2.sendPacket(this.getPlayer());
               wrapperPlayServerEntityEquipment3.sendPacket(this.getPlayer());
               wrapperPlayServerEntityEquipment4.sendPacket(this.getPlayer());
               wrapperPlayServerEntityEquipment5.sendPacket(this.getPlayer());
            }

            this.unusedFlag = false;
         }

         if (AAC.random.nextInt(100) < 7 && (this.plugin.combatLogoutListener.isInCombat(this.getPlayer()) || !invisibleOutsidePvp) && !headPlayerInvisible) {
            WrapperPlayServerEntityEquipment wrapperPlayServerEntityEquipment6 = new WrapperPlayServerEntityEquipment();
            WrapperPlayServerEntityEquipment wrapperPlayServerEntityEquipment7 = new WrapperPlayServerEntityEquipment();
            WrapperPlayServerEntityEquipment wrapperPlayServerEntityEquipment8 = new WrapperPlayServerEntityEquipment();
            WrapperPlayServerEntityEquipment wrapperPlayServerEntityEquipment9 = new WrapperPlayServerEntityEquipment();
            WrapperPlayServerEntityEquipment wrapperPlayServerEntityEquipment10 = new WrapperPlayServerEntityEquipment();
            wrapperPlayServerEntityEquipment6.setEntityID(this.headEntityId);
            wrapperPlayServerEntityEquipment7.setEntityID(this.headEntityId);
            wrapperPlayServerEntityEquipment8.setEntityID(this.headEntityId);
            wrapperPlayServerEntityEquipment9.setEntityID(this.headEntityId);
            wrapperPlayServerEntityEquipment10.setEntityID(this.headEntityId);
            wrapperPlayServerEntityEquipment6.setSlot(0);
            wrapperPlayServerEntityEquipment7.setSlot(1);
            wrapperPlayServerEntityEquipment8.setSlot(2);
            wrapperPlayServerEntityEquipment9.setSlot(3);
            wrapperPlayServerEntityEquipment10.setSlot(4);
            wrapperPlayServerEntityEquipment6.setItem(this.randomWeapon());
            wrapperPlayServerEntityEquipment7.setItem(this.randomBoots());
            wrapperPlayServerEntityEquipment8.setItem(this.randomLeggings());
            wrapperPlayServerEntityEquipment9.setItem(this.randomChestplate());
            wrapperPlayServerEntityEquipment10.setItem(this.randomHelmet());
            if (equipHeldItem) {
               wrapperPlayServerEntityEquipment6.sendPacket(this.getPlayer());
            }

            if (equipArmour) {
               wrapperPlayServerEntityEquipment7.sendPacket(this.getPlayer());
               wrapperPlayServerEntityEquipment8.sendPacket(this.getPlayer());
               wrapperPlayServerEntityEquipment9.sendPacket(this.getPlayer());
               wrapperPlayServerEntityEquipment10.sendPacket(this.getPlayer());
            }

            this.unusedFlag = true;
         }

         if (fakeOnGround) {
            WrapperPlayServerRelEntityMove wrapperPlayServerRelEntityMove = createRelMovePacket(this.headEntityId);
            wrapperPlayServerRelEntityMove.sendPacket(this.getPlayer());
            if (useSidePlayers) {
               WrapperPlayServerRelEntityMove wrapperPlayServerRelEntityMove2 = createRelMovePacket(this.rearEntityId);
               wrapperPlayServerRelEntityMove2.sendPacket(this.getPlayer());
               WrapperPlayServerRelEntityMove wrapperPlayServerRelEntityMove3 = createRelMovePacket(this.sideEntityId);
               wrapperPlayServerRelEntityMove3.sendPacket(this.getPlayer());
            }
         }
      }
   }

   private ItemStack randomWeapon() {
      switch (ThreadLocalRandom.current().nextInt(11)) {
         case 0:
         case 1:
            return new ItemStack(Material.WOOD_SWORD);
         case 2:
            return new ItemStack(Material.STONE_SWORD);
         case 3:
            return new ItemStack(Material.STICK);
         case 4:
         case 5:
         case 6:
            return new ItemStack(Material.IRON_SWORD);
         case 7:
            return new ItemStack(Material.DIAMOND_AXE);
         case 8:
            return new ItemStack(Material.DIAMOND_SWORD);
         case 9:
            return new ItemStack(Material.WOOD_AXE);
         case 10:
            return new ItemStack(Material.IRON_AXE);
         default:
            throw new AssertionError();
      }
   }

   private ItemStack randomBoots() {
      switch (ThreadLocalRandom.current().nextInt(9)) {
         case 0:
         case 1:
            return new ItemStack(Material.IRON_BOOTS);
         case 2:
            return new ItemStack(Material.DIAMOND_BOOTS);
         case 3:
            return new ItemStack(Material.LEATHER_BOOTS);
         case 4:
            return new ItemStack(Material.CHAINMAIL_BOOTS);
         case 5:
            return new ItemStack(Material.GOLD_BOOTS);
         case 6:
         case 7:
         case 8:
            return new ItemStack(Material.AIR);
         default:
            throw new AssertionError();
      }
   }

   private ItemStack randomLeggings() {
      switch (ThreadLocalRandom.current().nextInt(9)) {
         case 0:
         case 1:
            return new ItemStack(Material.IRON_LEGGINGS);
         case 2:
            return new ItemStack(Material.DIAMOND_LEGGINGS);
         case 3:
            return new ItemStack(Material.LEATHER_LEGGINGS);
         case 4:
            return new ItemStack(Material.CHAINMAIL_LEGGINGS);
         case 5:
            return new ItemStack(Material.GOLD_LEGGINGS);
         case 6:
         case 7:
         case 8:
            return new ItemStack(Material.AIR);
         default:
            throw new AssertionError();
      }
   }

   private ItemStack randomChestplate() {
      switch (ThreadLocalRandom.current().nextInt(9)) {
         case 0:
         case 1:
            return new ItemStack(Material.IRON_CHESTPLATE);
         case 2:
            return new ItemStack(Material.DIAMOND_CHESTPLATE);
         case 3:
            return new ItemStack(Material.LEATHER_CHESTPLATE);
         case 4:
            return new ItemStack(Material.CHAINMAIL_CHESTPLATE);
         case 5:
            return new ItemStack(Material.GOLD_CHESTPLATE);
         case 6:
         case 7:
         case 8:
            return new ItemStack(Material.AIR);
         default:
            throw new AssertionError();
      }
   }

   private ItemStack randomHelmet() {
      switch (ThreadLocalRandom.current().nextInt(9)) {
         case 0:
         case 1:
            return new ItemStack(Material.IRON_HELMET);
         case 2:
            return new ItemStack(Material.DIAMOND_HELMET);
         case 3:
            return new ItemStack(Material.LEATHER_HELMET);
         case 4:
            return new ItemStack(Material.CHAINMAIL_HELMET);
         case 5:
            return new ItemStack(Material.GOLD_HELMET);
         case 6:
            return new ItemStack(Material.PUMPKIN);
         case 7:
            return new ItemStack(Material.SKULL_ITEM);
         case 8:
            return new ItemStack(Material.AIR);
         default:
            throw new AssertionError();
      }
   }

   public void destroy() {
      this.forcefield.removeDecoyManager(this.playerId);
      if (this.getPlayer() != null) {
         if (headTablist && !this.useOnlineName) {
            WrapperPlayServerPlayerInfo wrapperPlayServerPlayerInfo = createPlayerInfoPacket(PlayerInfoAction.REMOVE_PLAYER, pastPlayerNames, this.headIdentity);
            wrapperPlayServerPlayerInfo.sendPacket(this.getPlayer());
         }

         this.getNextTaskId();
         this.tickTask.cancel();
         Bukkit.getServer().getScheduler().cancelTask(this.secondSpawnTaskId);
      }
   }

   private Integer getNextTaskId() {
      Player player = this.getPlayer();
      if (player == null) {
         return 0;
      }

      for (Entry entry : this.decoyVisibilityFlags.entrySet()) {
         if (this.getPlayer().isOnline()) {
            this.createDestroyPacket((Integer)entry.getKey()).sendPacket(this.getPlayer());
         }

         if ((Boolean)entry.getValue()) {
            this.unusedEntityId++;
         }
      }

      this.decoyVisibilityFlags.clear();
      int value = this.unusedEntityId;
      return value;
   }

   public int getHeadEntityId() {
      return this.headEntityId;
   }

   public static WrapperPlayServerNamedEntitySpawn createSpawnPacket(Vector vector, boolean flag, PlayerIdentity playerIdentity, Player player) {
      int value = allocateEntityId(player);
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

   public static WrapperPlayServerPlayerInfo createPlayerInfoPacket(PlayerInfoAction playerInfoAction, boolean flag, PlayerIdentity playerIdentity) {
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

   public static WrapperPlayServerEntityMetadata createMetadataPacket(int i, boolean flag, boolean flag2) {
      byte b = (byte)((flag ? 32 : 0) | (randomSprint ? (flag2 ? 8 : 0) : 0));
      WrappedWatchableObject wrappedWatchableObject = new WrappedWatchableObject(0, b);
      WrappedWatchableObject wrappedWatchableObject2 = new WrappedWatchableObject(6, AAC.random.nextInt(19) + 1.0F);
      WrapperPlayServerEntityMetadata wrapperPlayServerEntityMetadata = new WrapperPlayServerEntityMetadata();
      wrapperPlayServerEntityMetadata.setEntityID(i);
      ArrayList list = new ArrayList();
      list.add(wrappedWatchableObject);
      list.add(wrappedWatchableObject2);
      wrapperPlayServerEntityMetadata.setMetadata(list);
      return wrapperPlayServerEntityMetadata;
   }

   public static WrapperPlayServerRelEntityMove createRelMovePacket(int i) {
      WrapperPlayServerRelEntityMove wrapperPlayServerRelEntityMove = new WrapperPlayServerRelEntityMove();
      wrapperPlayServerRelEntityMove.setEntityID(i);
      wrapperPlayServerRelEntityMove.setOnGround(true);
      wrapperPlayServerRelEntityMove.setDx(0.0);
      wrapperPlayServerRelEntityMove.setDy(0.0);
      wrapperPlayServerRelEntityMove.setDz(0.0);
      return wrapperPlayServerRelEntityMove;
   }

   public static WrapperPlayServerEntityStatus createStatusPacket(int i) {
      WrapperPlayServerEntityStatus wrapperPlayServerEntityStatus = new WrapperPlayServerEntityStatus();
      wrapperPlayServerEntityStatus.setEntityID(i);
      wrapperPlayServerEntityStatus.setStatus((byte)2);
      return wrapperPlayServerEntityStatus;
   }

   private static int allocateEntityId(Player player) {
      return player.getEntityId() + 1073741823 + ThreadLocalRandom.current().nextInt(100000);
   }

   static Player getPlayer(ForcefieldDecoyManager forcefieldDecoyManager) {
      return forcefieldDecoyManager.getPlayer();
   }

   static boolean isOnlinePlayerNames() {
      return onlinePlayerNames;
   }

   static PlayerIdentity setHeadIdentity(ForcefieldDecoyManager forcefieldDecoyManager, PlayerIdentity playerIdentity) {
      return forcefieldDecoyManager.headIdentity = playerIdentity;
   }

   static UUID getPlayerId(ForcefieldDecoyManager forcefieldDecoyManager) {
      return forcefieldDecoyManager.playerId;
   }

   static PlayerIdentity getHeadIdentity(ForcefieldDecoyManager forcefieldDecoyManager) {
      return forcefieldDecoyManager.headIdentity;
   }

   static boolean setUseOnlineName(ForcefieldDecoyManager forcefieldDecoyManager, boolean flag) {
      return forcefieldDecoyManager.useOnlineName = flag;
   }

   static boolean isPastPlayerNames() {
      return pastPlayerNames;
   }

   static double getHeadPlayerHeight() {
      return headPlayerHeight;
   }

   static boolean isHeadPlayerInvisible() {
      return headPlayerInvisible;
   }

   static boolean isUseOnlineName(ForcefieldDecoyManager forcefieldDecoyManager) {
      return forcefieldDecoyManager.useOnlineName;
   }

   static Map getDecoyVisibilityFlags(ForcefieldDecoyManager forcefieldDecoyManager) {
      return forcefieldDecoyManager.decoyVisibilityFlags;
   }

   static boolean isHeadTablist() {
      return headTablist;
   }

   static int setHeadEntityId(ForcefieldDecoyManager forcefieldDecoyManager, int i) {
      return forcefieldDecoyManager.headEntityId = i;
   }

   static boolean isUseSidePlayers() {
      return useSidePlayers;
   }

   static PlayerIdentity setRearIdentity(ForcefieldDecoyManager forcefieldDecoyManager, PlayerIdentity playerIdentity) {
      return forcefieldDecoyManager.rearIdentity = playerIdentity;
   }

   static PlayerIdentity getRearIdentity(ForcefieldDecoyManager forcefieldDecoyManager) {
      return forcefieldDecoyManager.rearIdentity;
   }

   static int setRearEntityId(ForcefieldDecoyManager forcefieldDecoyManager, int i) {
      return forcefieldDecoyManager.rearEntityId = i;
   }

   static PlayerIdentity setSideIdentity(ForcefieldDecoyManager forcefieldDecoyManager, PlayerIdentity playerIdentity) {
      return forcefieldDecoyManager.sideIdentity = playerIdentity;
   }

   static PlayerIdentity getSideIdentity(ForcefieldDecoyManager forcefieldDecoyManager) {
      return forcefieldDecoyManager.sideIdentity;
   }

   static int setSideEntityId(ForcefieldDecoyManager forcefieldDecoyManager, int i) {
      return forcefieldDecoyManager.sideEntityId = i;
   }

   static int setSecondSpawnTaskId(ForcefieldDecoyManager forcefieldDecoyManager, int i) {
      return forcefieldDecoyManager.secondSpawnTaskId = i;
   }

   static AAC getPlugin(ForcefieldDecoyManager forcefieldDecoyManager) {
      return forcefieldDecoyManager.plugin;
   }

   static int getMaxTicks(ForcefieldDecoyManager forcefieldDecoyManager) {
      return forcefieldDecoyManager.maxTicks;
   }

   static void scheduleTick(ForcefieldDecoyManager forcefieldDecoyManager) {
      forcefieldDecoyManager.scheduleTick();
   }

   static void tickPositions(ForcefieldDecoyManager forcefieldDecoyManager) {
      forcefieldDecoyManager.tickPositions();
   }

   static int addElapsedTicks(ForcefieldDecoyManager forcefieldDecoyManager, int i) {
      return forcefieldDecoyManager.elapsedTicks += i;
   }

   static int getTickPeriod(ForcefieldDecoyManager forcefieldDecoyManager) {
      return forcefieldDecoyManager.tickPeriod;
   }

   static int getElapsedTicks(ForcefieldDecoyManager forcefieldDecoyManager) {
      return forcefieldDecoyManager.elapsedTicks;
   }

   private static class SpawnTaskDeferredSpawn implements Runnable {
      final SpawnTask spawnTask;

      SpawnTaskDeferredSpawn(SpawnTask spawnTask) {
         this.spawnTask = spawnTask;
      }

      @Override
      public void run() {
         this.spawnTask.decoyManager.destroy();
      }
   }

   private static class SpawnTask implements Runnable {
      final ForcefieldDecoyManager decoyManager;

      SpawnTask(ForcefieldDecoyManager forcefieldDecoyManager) {
         this.decoyManager = forcefieldDecoyManager;
      }

      @Override
      public void run() {
         if (ForcefieldDecoyManager.getPlayer(this.decoyManager) != null) {
            if (ForcefieldDecoyManager.isOnlinePlayerNames()) {
               ForcefieldDecoyManager.setHeadIdentity(this.decoyManager, PlayerIdentityPool.getInstance().getRandomOnlineIdentity(ForcefieldDecoyManager.getPlayerId(this.decoyManager)));
               if (ForcefieldDecoyManager.getHeadIdentity(this.decoyManager) == null) {
                  boolean flag = false;
                  ForcefieldDecoyManager forcefieldDecoyManager = this.decoyManager;
                  ForcefieldDecoyManager.setUseOnlineName(forcefieldDecoyManager, flag);
                  ForcefieldDecoyManager.setHeadIdentity(this.decoyManager, PlayerIdentityPool.getInstance().getRandomUnusedIdentity(ForcefieldDecoyManager.getPlayerId(this.decoyManager)));
               }
            } else if (ForcefieldDecoyManager.isPastPlayerNames()) {
               ForcefieldDecoyManager.setHeadIdentity(this.decoyManager, PlayerIdentityPool.getInstance().getRandomUnusedIdentity(ForcefieldDecoyManager.getPlayerId(this.decoyManager)));
            } else {
               ForcefieldDecoyManager.setHeadIdentity(this.decoyManager, PlayerIdentityPool.getInstance().generateRandomIdentity());
            }

            WrapperPlayServerNamedEntitySpawn wrapperPlayServerNamedEntitySpawn = ForcefieldDecoyManager.createSpawnPacket(
               ForcefieldDecoyManager.getPlayer(this.decoyManager)
                  .getLocation()
                  .add(AAC.random.nextFloat() - 0.5, ForcefieldDecoyManager.getHeadPlayerHeight() * 1.7, AAC.random.nextFloat() - 0.5)
                  .toVector(),
               ForcefieldDecoyManager.isHeadPlayerInvisible(),
               ForcefieldDecoyManager.getHeadIdentity(this.decoyManager),
               ForcefieldDecoyManager.getPlayer(this.decoyManager)
            );
            if (!ForcefieldDecoyManager.isUseOnlineName(this.decoyManager)) {
               WrapperPlayServerPlayerInfo wrapperPlayServerPlayerInfo = ForcefieldDecoyManager.createPlayerInfoPacket(
                  PlayerInfoAction.ADD_PLAYER, ForcefieldDecoyManager.isPastPlayerNames(), ForcefieldDecoyManager.getHeadIdentity(this.decoyManager)
               );
               wrapperPlayServerPlayerInfo.sendPacket(ForcefieldDecoyManager.getPlayer(this.decoyManager));
            }

            wrapperPlayServerNamedEntitySpawn.sendPacket(ForcefieldDecoyManager.getPlayer(this.decoyManager));
            Map map = ForcefieldDecoyManager.getDecoyVisibilityFlags(this.decoyManager);
            Integer intVal = wrapperPlayServerNamedEntitySpawn.getEntityID();
            boolean flag2 = false;
            map.put(intVal, flag2);
            if (!ForcefieldDecoyManager.isHeadTablist() && !ForcefieldDecoyManager.isUseOnlineName(this.decoyManager)) {
               WrapperPlayServerPlayerInfo wrapperPlayServerPlayerInfo2 = ForcefieldDecoyManager.createPlayerInfoPacket(
                  PlayerInfoAction.REMOVE_PLAYER, ForcefieldDecoyManager.isPastPlayerNames(), ForcefieldDecoyManager.getHeadIdentity(this.decoyManager)
               );
               wrapperPlayServerPlayerInfo2.sendPacket(ForcefieldDecoyManager.getPlayer(this.decoyManager));
            }

            ForcefieldDecoyManager.setHeadEntityId(this.decoyManager, wrapperPlayServerNamedEntitySpawn.getEntityID());
            if (ForcefieldDecoyManager.isUseSidePlayers()) {
               if (ForcefieldDecoyManager.isPastPlayerNames()) {
                  ForcefieldDecoyManager.setRearIdentity(this.decoyManager, PlayerIdentityPool.getInstance().getRandomUnusedIdentity(ForcefieldDecoyManager.getPlayer(this.decoyManager).getUniqueId()));
               } else {
                  ForcefieldDecoyManager.setRearIdentity(this.decoyManager, PlayerIdentityPool.getInstance().generateRandomIdentity());
               }

               int yaw = (int)ForcefieldDecoyManager.getPlayer(this.decoyManager).getLocation().getYaw() + 190;
               double d = Math.toRadians(yaw);
               Vector vector = ForcefieldDecoyManager.getPlayer(this.decoyManager)
                  .getLocation()
                  .add(1.7 * Math.cos(d), AAC.random.nextFloat() / 2.0F - 1.0F, 1.7 * Math.sin(d))
                  .toVector();
               Player player = ForcefieldDecoyManager.getPlayer(this.decoyManager);
               PlayerIdentity playerIdentity = ForcefieldDecoyManager.getRearIdentity(this.decoyManager);
               boolean flag3 = true;
               Vector vector2 = vector;
               WrapperPlayServerNamedEntitySpawn wrapperPlayServerNamedEntitySpawn2 = ForcefieldDecoyManager.createSpawnPacket(vector2, flag3, playerIdentity, player);
               WrapperPlayServerPlayerInfo wrapperPlayServerPlayerInfo3 = ForcefieldDecoyManager.createPlayerInfoPacket(
                  PlayerInfoAction.ADD_PLAYER, ForcefieldDecoyManager.isPastPlayerNames(), ForcefieldDecoyManager.getRearIdentity(this.decoyManager)
               );
               wrapperPlayServerPlayerInfo3.sendPacket(ForcefieldDecoyManager.getPlayer(this.decoyManager));
               wrapperPlayServerNamedEntitySpawn2.sendPacket(ForcefieldDecoyManager.getPlayer(this.decoyManager));
               map = ForcefieldDecoyManager.getDecoyVisibilityFlags(this.decoyManager);
               intVal = wrapperPlayServerNamedEntitySpawn2.getEntityID();
               boolean flag4 = false;
               map.put(intVal, flag4);
               WrapperPlayServerPlayerInfo wrapperPlayServerPlayerInfo4 = ForcefieldDecoyManager.createPlayerInfoPacket(
                  PlayerInfoAction.REMOVE_PLAYER, ForcefieldDecoyManager.isPastPlayerNames(), ForcefieldDecoyManager.getRearIdentity(this.decoyManager)
               );
               wrapperPlayServerPlayerInfo4.sendPacket(ForcefieldDecoyManager.getPlayer(this.decoyManager));
               ForcefieldDecoyManager.setRearEntityId(this.decoyManager, wrapperPlayServerNamedEntitySpawn2.getEntityID());
               if (ForcefieldDecoyManager.isPastPlayerNames()) {
                  ForcefieldDecoyManager.setSideIdentity(this.decoyManager, PlayerIdentityPool.getInstance().getRandomUnusedIdentity(ForcefieldDecoyManager.getPlayer(this.decoyManager).getUniqueId()));
               } else {
                  ForcefieldDecoyManager.setSideIdentity(this.decoyManager, PlayerIdentityPool.getInstance().generateRandomIdentity());
               }

               yaw = (int)ForcefieldDecoyManager.getPlayer(this.decoyManager).getLocation().getYaw() + 10;
               d = Math.toRadians(yaw);
               Vector vector3 = ForcefieldDecoyManager.getPlayer(this.decoyManager)
                  .getLocation()
                  .add(1.7 * Math.cos(d), AAC.random.nextFloat() / 2.0F - 1.0F, 1.7 * Math.sin(d))
                  .toVector();
               Player player2 = ForcefieldDecoyManager.getPlayer(this.decoyManager);
               PlayerIdentity playerIdentity2 = ForcefieldDecoyManager.getSideIdentity(this.decoyManager);
               boolean flag5 = true;
               Vector vector4 = vector3;
               WrapperPlayServerNamedEntitySpawn wrapperPlayServerNamedEntitySpawn3 = ForcefieldDecoyManager.createSpawnPacket(vector4, flag5, playerIdentity2, player2);
               WrapperPlayServerPlayerInfo wrapperPlayServerPlayerInfo5 = ForcefieldDecoyManager.createPlayerInfoPacket(
                  PlayerInfoAction.ADD_PLAYER, ForcefieldDecoyManager.isPastPlayerNames(), ForcefieldDecoyManager.getSideIdentity(this.decoyManager)
               );
               wrapperPlayServerPlayerInfo5.sendPacket(ForcefieldDecoyManager.getPlayer(this.decoyManager));
               wrapperPlayServerNamedEntitySpawn3.sendPacket(ForcefieldDecoyManager.getPlayer(this.decoyManager));
               map = ForcefieldDecoyManager.getDecoyVisibilityFlags(this.decoyManager);
               intVal = wrapperPlayServerNamedEntitySpawn3.getEntityID();
               boolean flag6 = false;
               map.put(intVal, flag6);
               WrapperPlayServerPlayerInfo wrapperPlayServerPlayerInfo6 = ForcefieldDecoyManager.createPlayerInfoPacket(
                  PlayerInfoAction.REMOVE_PLAYER, ForcefieldDecoyManager.isPastPlayerNames(), ForcefieldDecoyManager.getSideIdentity(this.decoyManager)
               );
               wrapperPlayServerPlayerInfo6.sendPacket(ForcefieldDecoyManager.getPlayer(this.decoyManager));
               ForcefieldDecoyManager.setSideEntityId(this.decoyManager, wrapperPlayServerNamedEntitySpawn3.getEntityID());
            }

            ForcefieldDecoyManager.setSecondSpawnTaskId(
               this.decoyManager,
               Bukkit.getScheduler()
                  .scheduleSyncDelayedTask(
                     ForcefieldDecoyManager.getPlugin(this.decoyManager), new SpawnTaskDeferredSpawn(this), ForcefieldDecoyManager.getMaxTicks(this.decoyManager)
                  )
            );
            ForcefieldDecoyManager.scheduleTick(this.decoyManager);
         }
      }
   }

   private static class TickTask implements Runnable {
      final ForcefieldDecoyManager decoyManager;

      TickTask(ForcefieldDecoyManager forcefieldDecoyManager) {
         this.decoyManager = forcefieldDecoyManager;
      }

      @Override
      public void run() {
         ForcefieldDecoyManager.tickPositions(this.decoyManager);
         ForcefieldDecoyManager.addElapsedTicks(this.decoyManager, ForcefieldDecoyManager.getTickPeriod(this.decoyManager));
         if (ForcefieldDecoyManager.getElapsedTicks(this.decoyManager) < ForcefieldDecoyManager.getMaxTicks(this.decoyManager) - ForcefieldDecoyManager.getTickPeriod(this.decoyManager)) {
            ForcefieldDecoyManager.scheduleTick(this.decoyManager);
         }
      }
   }
}
