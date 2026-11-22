package me.konsolas.aac.internal.packetwrapper;

import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.World;
import org.bukkit.entity.Entity;

public class WrapperPlayServerRelEntityMove extends AbstractPacket {
   public static final PacketType TYPE = Server.REL_ENTITY_MOVE;

   public WrapperPlayServerRelEntityMove() {
      super(new PacketContainer(TYPE), TYPE);
      this.handle.getModifier().writeDefaults();
   }

   public WrapperPlayServerRelEntityMove(PacketContainer packet) {
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

   public double getDx() {
      return ((Byte)this.handle.getBytes().read(0)).byteValue() / 32.0;
   }

   public void setDx(double d) {
      this.handle.getBytes().write(0, (byte)(d * 32.0));
   }

   public double getDy() {
      return ((Byte)this.handle.getBytes().read(1)).byteValue() / 32.0;
   }

   public void setDy(double d) {
      this.handle.getBytes().write(1, (byte)(d * 32.0));
   }

   public double getDz() {
      return ((Byte)this.handle.getBytes().read(2)).byteValue() / 32.0;
   }

   public void setDz(double d) {
      this.handle.getBytes().write(2, (byte)(d * 32.0));
   }

   public boolean getOnGround() {
      return (Boolean)this.handle.getSpecificModifier(boolean.class).read(0);
   }

   public void setOnGround(boolean flag) {
      this.handle.getSpecificModifier(boolean.class).write(0, flag);
   }
}
