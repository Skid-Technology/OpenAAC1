package me.konsolas.aac.detectors.movement;

import com.comphenix.protocol.PacketType.Play.Client;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import me.konsolas.aac.AAC;
import me.konsolas.aac.api.HackType;
import me.konsolas.aac.detectors.Detector;
import org.bukkit.plugin.Plugin;

public final class HeadRoll extends Detector {
   public HeadRoll(AAC plugin) {
      super(plugin);
      HeadLookPacketListener headLookPacketListener = new HeadLookPacketListener(this, this.plugin, Client.LOOK, Client.POSITION_LOOK);
      this.packetListeners.add(headLookPacketListener);
      ProtocolLibrary.getProtocolManager().addPacketListener(headLookPacketListener);
   }


   private static class HeadLookPacketListener extends PacketAdapter {
      final HeadRoll headRoll;

      HeadLookPacketListener(HeadRoll headRoll, Plugin plugin, PacketType... packetTypeArray) {
         super(plugin, packetTypeArray);
         this.headRoll = headRoll;
      }

      public void onPacketReceiving(PacketEvent event) {
         if (event.getPacketType() == Client.LOOK || event.getPacketType() == Client.POSITION_LOOK) {
            try {
               if (((AAC)this.plugin).checkManager.isPlayerExempt(event.getPlayer())) {
                  return;
               }

               float f = (Float)event.getPacket().getFloat().read(1);
               if (f > 90.1F || f < -90.1F) {
                  event.setCancelled(true);
                  this.headRoll.recordViolation(event.getPlayer().getUniqueId(), 1, HackType.HEADROLL, event.getPlayer().getName() + " failed HeadRoll: Moved with an illegal pitch (" + f + ")");
               }
            } catch (NullPointerException ex) {
            }
         }
      }
   }
}
