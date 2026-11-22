package me.konsolas.aac.internal.packetwrapper;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.google.common.base.Objects;
import java.lang.reflect.InvocationTargetException;
import org.bukkit.entity.Player;

public abstract class AbstractPacket {
   protected PacketContainer handle;

   protected AbstractPacket(PacketContainer packet, PacketType packetType) {
      if (packet == null) {
         throw new IllegalArgumentException("Packet handle cannot be NULL.");
      }

      if (!Objects.equal(packet.getType(), packetType)) {
         throw new IllegalArgumentException(packet.getHandle() + " is not a packet of type " + packetType);
      }

      this.handle = packet;
   }

   public PacketContainer getHandle() {
      return this.handle;
   }

   public void sendPacket(Player player) {
      try {
         ProtocolLibrary.getProtocolManager().sendServerPacket(player, this.getHandle());
      } catch (InvocationTargetException ex) {
         throw new RuntimeException("Cannot send packet.", ex);
      }
   }

   public void receivePacket(Player player) {
      try {
         ProtocolLibrary.getProtocolManager().recieveClientPacket(player, this.getHandle());
      } catch (Exception ex) {
         throw new RuntimeException("Cannot recieve packet.", ex);
      }
   }
}
