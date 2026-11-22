package me.konsolas.aac.internal.packetwrapper;

import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import java.util.List;

public class WrapperPlayServerPlayerInfo extends AbstractPacket {
   public static final PacketType TYPE = Server.PLAYER_INFO;

   public WrapperPlayServerPlayerInfo() {
      super(new PacketContainer(TYPE), TYPE);
      this.handle.getModifier().writeDefaults();
   }

   public WrapperPlayServerPlayerInfo(PacketContainer packet) {
      super(packet, TYPE);
   }

   public PlayerInfoAction getAction() {
      return (PlayerInfoAction)this.handle.getPlayerInfoAction().read(0);
   }

   public void setAction(PlayerInfoAction playerInfoAction) {
      this.handle.getPlayerInfoAction().write(0, playerInfoAction);
   }

   public List<PlayerInfoData> getData() {
      return (List<PlayerInfoData>)this.handle.getPlayerInfoDataLists().read(0);
   }

   public void setData(List<PlayerInfoData> list) {
      this.handle.getPlayerInfoDataLists().write(0, list);
   }
}
