package me.konsolas.aac.internal.packetwrapper;

import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import java.util.UUID;
import org.bukkit.util.Vector;

public class WrapperPlayServerNamedEntitySpawn extends AbstractPacket {
   public static final PacketType TYPE = Server.NAMED_ENTITY_SPAWN;

   public WrapperPlayServerNamedEntitySpawn() {
      super(new PacketContainer(TYPE), TYPE);
      this.handle.getModifier().writeDefaults();
   }

   public WrapperPlayServerNamedEntitySpawn(PacketContainer packet) {
      super(packet, TYPE);
   }

   public int getEntityID() {
      return (Integer)this.handle.getIntegers().read(0);
   }

   public void setEntityID(int i) {
      this.handle.getIntegers().write(0, i);
   }

   public UUID getPlayerUUID() {
      return (UUID)this.handle.getSpecificModifier(UUID.class).read(0);
   }

   public void setPlayerUUID(UUID uuid) {
      this.handle.getSpecificModifier(UUID.class).write(0, uuid);
   }

   public Vector getPosition() {
      return new Vector(this.getX(), this.getY(), this.getZ());
   }

   public void setPosition(Vector vector) {
      this.setX(vector.getX());
      this.setY(vector.getY());
      this.setZ(vector.getZ());
   }

   public double getX() {
      return ((Integer)this.handle.getIntegers().read(1)).intValue() / 32.0;
   }

   public void setX(double d) {
      this.handle.getIntegers().write(1, (int)Math.floor(d * 32.0));
   }

   public double getY() {
      return ((Integer)this.handle.getIntegers().read(2)).intValue() / 32.0;
   }

   public void setY(double d) {
      this.handle.getIntegers().write(2, (int)Math.floor(d * 32.0));
   }

   public double getZ() {
      return ((Integer)this.handle.getIntegers().read(3)).intValue() / 32.0;
   }

   public void setZ(double d) {
      this.handle.getIntegers().write(3, (int)Math.floor(d * 32.0));
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

   public int getCurrentItem() {
      return (Integer)this.handle.getIntegers().read(4);
   }

   public void setCurrentItem(int i) {
      this.handle.getIntegers().write(4, i);
   }

   public WrappedDataWatcher getMetadata() {
      return (WrappedDataWatcher)this.handle.getDataWatcherModifier().read(0);
   }

   public void setMetadata(WrappedDataWatcher wrappedDataWatcher) {
      this.handle.getDataWatcherModifier().write(0, wrappedDataWatcher);
   }
}
