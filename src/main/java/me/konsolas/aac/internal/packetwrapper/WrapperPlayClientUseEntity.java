package me.konsolas.aac.internal.packetwrapper;

import com.comphenix.protocol.PacketType.Play.Client;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers.EntityUseAction;
import org.bukkit.util.Vector;

public class WrapperPlayClientUseEntity extends AbstractPacket {
   public static final PacketType TYPE = Client.USE_ENTITY;

   public WrapperPlayClientUseEntity() {
      super(new PacketContainer(TYPE), TYPE);
      this.handle.getModifier().writeDefaults();
   }

   public WrapperPlayClientUseEntity(PacketContainer packet) {
      super(packet, TYPE);
   }

   public int getTargetID() {
      return (Integer)this.handle.getIntegers().read(0);
   }

   public void setTargetID(int i) {
      this.handle.getIntegers().write(0, i);
   }

   public EntityUseAction getType() {
      return (EntityUseAction)this.handle.getEntityUseActions().read(0);
   }

   public void setType(EntityUseAction entityUseAction) {
      this.handle.getEntityUseActions().write(0, entityUseAction);
   }

   public Vector getTargetVector() {
      return (Vector)this.handle.getVectors().read(0);
   }

   public void setTargetVector(Vector vector) {
      this.handle.getVectors().write(0, vector);
   }
}
