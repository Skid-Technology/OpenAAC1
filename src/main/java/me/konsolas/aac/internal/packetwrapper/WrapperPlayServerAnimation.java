package me.konsolas.aac.internal.packetwrapper;

import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.World;
import org.bukkit.entity.Entity;

public class WrapperPlayServerAnimation extends AbstractPacket {
   public static final PacketType TYPE = Server.ANIMATION;

   public WrapperPlayServerAnimation() {
      super(new PacketContainer(TYPE), TYPE);
      this.handle.getModifier().writeDefaults();
   }

   public WrapperPlayServerAnimation(PacketContainer packet) {
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

   public int getAnimation() {
      return (Integer)this.handle.getIntegers().read(1);
   }

   public void setAnimation(int i) {
      this.handle.getIntegers().write(1, i);
   }
}
