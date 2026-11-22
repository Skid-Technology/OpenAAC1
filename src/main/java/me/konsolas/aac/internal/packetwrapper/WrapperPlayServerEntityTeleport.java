package me.konsolas.aac.internal.packetwrapper;

import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.World;
import org.bukkit.entity.Entity;

public class WrapperPlayServerEntityTeleport extends AbstractPacket {
   public static final PacketType TYPE = Server.ENTITY_TELEPORT;

   public WrapperPlayServerEntityTeleport() {
      super(new PacketContainer(TYPE), TYPE);
      this.handle.getModifier().writeDefaults();
   }

   public WrapperPlayServerEntityTeleport(PacketContainer packet) {
      super(packet, TYPE);
   }

   public int getEntityID() {
      return (Integer)this.handle.getIntegers().read(0);
   }

   public void setEntityID(int i) {
      this.handle.getIntegers().write(0, i);
   }

   public Entity getEntity(World world) {
      return (Entity)this.handle.getEntityModifier(world).read(0);
   }

   public Entity getEntity(PacketEvent event) {
      return this.getEntity(event.getPlayer().getWorld());
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
}
