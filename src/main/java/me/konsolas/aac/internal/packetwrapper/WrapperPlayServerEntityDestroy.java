package me.konsolas.aac.internal.packetwrapper;

import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;

public class WrapperPlayServerEntityDestroy extends AbstractPacket {
   public static final PacketType TYPE = Server.ENTITY_DESTROY;

   public WrapperPlayServerEntityDestroy() {
      super(new PacketContainer(TYPE), TYPE);
      this.handle.getModifier().writeDefaults();
   }

   public WrapperPlayServerEntityDestroy(PacketContainer packet) {
      super(packet, TYPE);
   }

   public int getCount() {
      return ((int[])this.handle.getIntegerArrays().read(0)).length;
   }

   public int[] getEntityIDs() {
      return (int[])this.handle.getIntegerArrays().read(0);
   }

   public void setEntityIds(int[] valueArray) {
      this.handle.getIntegerArrays().write(0, valueArray);
   }
}
