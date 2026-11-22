package me.konsolas.aac.internal.packetwrapper;

import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.EnumWrappers;
import java.util.Set;

public class WrapperPlayServerPosition extends AbstractPacket {
   public static final PacketType TYPE = Server.POSITION;
   private static final Class<?> FLAGS_CLASS = MinecraftReflection.getMinecraftClass(
      "EnumPlayerTeleportFlags", new String[]{"PacketPlayOutPosition$EnumPlayerTeleportFlags"}
   );

   public WrapperPlayServerPosition() {
      super(new PacketContainer(TYPE), TYPE);
      this.handle.getModifier().writeDefaults();
   }

   public WrapperPlayServerPosition(PacketContainer packet) {
      super(packet, TYPE);
   }

   public double getX() {
      return (Double)this.handle.getDoubles().read(0);
   }

   public void setX(double d) {
      this.handle.getDoubles().write(0, d);
   }

   public double getY() {
      return (Double)this.handle.getDoubles().read(1);
   }

   public void setY(double d) {
      this.handle.getDoubles().write(1, d);
   }

   public double getZ() {
      return (Double)this.handle.getDoubles().read(2);
   }

   public void setZ(double d) {
      this.handle.getDoubles().write(2, d);
   }

   public float getYaw() {
      return (Float)this.handle.getFloat().read(0);
   }

   public void setYaw(float f) {
      this.handle.getFloat().write(0, f);
   }

   public float getPitch() {
      return (Float)this.handle.getFloat().read(1);
   }

   public void setPitch(float f) {
      this.handle.getFloat().write(1, f);
   }

   private StructureModifier<Set<PlayerTeleportFlag>> getFlagsModifier() {
      return this.handle
         .getModifier()
         .withType(Set.class, BukkitConverters.getSetConverter(FLAGS_CLASS, EnumWrappers.getGenericConverter(PlayerTeleportFlag.class)));
   }

   public Set<PlayerTeleportFlag> getFlags() {
      return (Set<PlayerTeleportFlag>)this.getFlagsModifier().read(0);
   }

   public void setFlags(Set<PlayerTeleportFlag> set) {
      this.getFlagsModifier().write(0, set);
   }

   public enum PlayerTeleportFlag {
      X,
      Y,
      Z,
      Y_ROT,
      X_ROT;

      private static final PlayerTeleportFlag[] VALUES = new PlayerTeleportFlag[]{X, Y, Z, Y_ROT, X_ROT};
   }
}
