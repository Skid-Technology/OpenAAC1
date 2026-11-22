package me.konsolas.aac.internal.packetwrapper;

import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.World;
import org.bukkit.entity.Entity;

public class WrapperPlayServerEntityStatus extends AbstractPacket {
   public static final PacketType TYPE = Server.ENTITY_STATUS;

   public WrapperPlayServerEntityStatus() {
      super(new PacketContainer(TYPE), TYPE);
      this.handle.getModifier().writeDefaults();
   }

   public WrapperPlayServerEntityStatus(PacketContainer packet) {
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

   public byte getStatus() {
      return (Byte)this.handle.getBytes().read(0);
   }

   public void setStatus(byte b) {
      this.handle.getBytes().write(0, b);
   }
}
