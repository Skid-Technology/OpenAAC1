package me.konsolas.aac.internal.packetwrapper;

import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.PacketConstructor;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class WrapperPlayServerSpawnEntityLiving extends AbstractPacket {
   public static final PacketType TYPE = Server.SPAWN_ENTITY_LIVING;
   private static PacketConstructor entityConstructor;

   public WrapperPlayServerSpawnEntityLiving() {
      super(new PacketContainer(TYPE), TYPE);
      this.handle.getModifier().writeDefaults();
   }

   public WrapperPlayServerSpawnEntityLiving(PacketContainer packet) {
      super(packet, TYPE);
   }

   public WrapperPlayServerSpawnEntityLiving(Entity entity) {
      super(fromEntity(entity), TYPE);
   }

   private static PacketContainer fromEntity(Entity entity) {
      if (entityConstructor == null) {
         entityConstructor = ProtocolLibrary.getProtocolManager().createPacketConstructor(TYPE, new Object[]{entity});
      }

      return entityConstructor.createPacket(new Object[]{entity});
   }

   public int getEntityID() {
      return (Integer)this.handle.getIntegers().read(0);
   }

   public Entity getEntity(World world) {
      return (Entity)this.handle.getEntityModifier(world).read(0);
   }

   public Entity getEntity(PacketEvent event) {
      return this.getEntity(event.getPlayer().getWorld());
   }

   public void setEntityID(int i) {
      this.handle.getIntegers().write(0, i);
   }

   public EntityType getType() {
      return EntityType.fromId((Integer)this.handle.getIntegers().read(1));
   }

   public void setType(EntityType entityType) {
      this.handle.getIntegers().write(1, Integer.valueOf(entityType.getTypeId()));
   }

   public double getX() {
      return ((Integer)this.handle.getIntegers().read(2)).intValue() / 32.0;
   }

   public void setX(double d) {
      this.handle.getIntegers().write(2, (int)Math.floor(d * 32.0));
   }

   public double getY() {
      return ((Integer)this.handle.getIntegers().read(3)).intValue() / 32.0;
   }

   public void setY(double d) {
      this.handle.getIntegers().write(3, (int)Math.floor(d * 32.0));
   }

   public double getZ() {
      return ((Integer)this.handle.getIntegers().read(4)).intValue() / 32.0;
   }

   public void setZ(double d) {
      this.handle.getIntegers().write(4, (int)Math.floor(d * 32.0));
   }

   public float getYaw() {
      return ((Byte)this.handle.getBytes().read(0)).byteValue() * 360.0F / 256.0F;
   }

   public void setYaw(float f) {
      this.handle.getBytes().write(0, (byte)(f * 256.0F / 360.0F));
   }

   public float getPitch() {
      return ((Byte)this.handle.getBytes().read(1)).byteValue() * 360.0F / 256.0F;
   }

   public void setPitch(float f) {
      this.handle.getBytes().write(1, (byte)(f * 256.0F / 360.0F));
   }

   public float getHeadPitch() {
      return ((Byte)this.handle.getBytes().read(2)).byteValue() * 360.0F / 256.0F;
   }

   public void setHeadPitch(float f) {
      this.handle.getBytes().write(2, (byte)(f * 256.0F / 360.0F));
   }

   public double getVelocityX() {
      return ((Integer)this.handle.getIntegers().read(5)).intValue() / 8000.0;
   }

   public void setVelocityX(double d) {
      this.handle.getIntegers().write(5, (int)(d * 8000.0));
   }

   public double getVelocityY() {
      return ((Integer)this.handle.getIntegers().read(6)).intValue() / 8000.0;
   }

   public void setVelocityY(double d) {
      this.handle.getIntegers().write(6, (int)(d * 8000.0));
   }

   public double getVelocityZ() {
      return ((Integer)this.handle.getIntegers().read(7)).intValue() / 8000.0;
   }

   public void setVelocityZ(double d) {
      this.handle.getIntegers().write(7, (int)(d * 8000.0));
   }

   public WrappedDataWatcher getMetadata() {
      return (WrappedDataWatcher)this.handle.getDataWatcherModifier().read(0);
   }

   public void setMetadata(WrappedDataWatcher wrappedDataWatcher) {
      this.handle.getDataWatcherModifier().write(0, wrappedDataWatcher);
   }
}
