package me.konsolas.aac.internal.packetwrapper;

import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public class WrapperPlayServerEntityVelocity extends AbstractPacket {
   public static final PacketType TYPE = Server.ENTITY_VELOCITY;

   public WrapperPlayServerEntityVelocity() {
      super(new PacketContainer(TYPE), TYPE);
      this.handle.getModifier().writeDefaults();
   }

   public WrapperPlayServerEntityVelocity(PacketContainer packet) {
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

   public double getVelocityX() {
      return ((Integer)this.handle.getIntegers().read(1)).intValue() / 8000.0;
   }

   public void setVelocityX(double d) {
      this.handle.getIntegers().write(1, (int)(d * 8000.0));
   }

   public double getVelocityY() {
      return ((Integer)this.handle.getIntegers().read(2)).intValue() / 8000.0;
   }

   public void setVelocityY(double d) {
      this.handle.getIntegers().write(2, (int)(d * 8000.0));
   }

   public double getVelocityZ() {
      return ((Integer)this.handle.getIntegers().read(3)).intValue() / 8000.0;
   }

   public void setVelocityZ(double d) {
      this.handle.getIntegers().write(3, (int)(d * 8000.0));
   }

   public Vector getVelocity() {
      return new Vector(this.getVelocityX(), this.getVelocityY(), this.getVelocityZ());
   }
}
