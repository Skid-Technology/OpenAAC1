package me.konsolas.aac.internal.packetwrapper;

import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;

public class WrapperPlayServerEntityMoveLook extends AbstractPacket {
   public static final PacketType TYPE = Server.ENTITY_MOVE_LOOK;

   public WrapperPlayServerEntityMoveLook() {
      super(new PacketContainer(TYPE), TYPE);
      this.handle.getModifier().writeDefaults();
   }

   public WrapperPlayServerEntityMoveLook(PacketContainer packet) {
      super(packet, TYPE);
   }

   public int getEntityID() {
      return (Integer)this.handle.getIntegers().read(0);
   }

   public void setEntityID(int i) {
      this.handle.getIntegers().write(0, i);
   }

   public double getDx() {
      return ((Byte)this.handle.getBytes().read(0)).byteValue() / 32.0;
   }

   public void setDx(double d) {
      this.handle.getBytes().write(0, (byte)(d * 32.0));
   }

   public double getDy() {
      return ((Byte)this.handle.getBytes().read(0)).byteValue() / 32.0;
   }

   public void setDy(double d) {
      this.handle.getBytes().write(0, (byte)(d * 32.0));
   }

   public double getDz() {
      return ((Byte)this.handle.getBytes().read(0)).byteValue() / 32.0;
   }

   public void setDz(double d) {
      this.handle.getBytes().write(0, (byte)(d * 32.0));
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

   public boolean getOnGround() {
      return (Boolean)this.handle.getSpecificModifier(boolean.class).read(0);
   }

   public void setOnGround(boolean flag) {
      this.handle.getSpecificModifier(boolean.class).write(0, flag);
   }
}
