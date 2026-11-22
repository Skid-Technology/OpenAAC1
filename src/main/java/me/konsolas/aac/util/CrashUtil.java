package me.konsolas.aac.util;

import com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import java.util.concurrent.ThreadLocalRandom;
import me.konsolas.aac.AAC;
import me.konsolas.aac.internal.packetwrapper.WrapperPlayServerNamedEntitySpawn;
import me.konsolas.aac.internal.packetwrapper.WrapperPlayServerPlayerInfo;
import me.konsolas.aac.internal.packetwrapper.WrapperPlayServerSpawnEntityLiving;
import me.konsolas.aac.util.general.RandomPlayerNameGenerator;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public final class CrashUtil {
   private CrashUtil() {
      throw new AssertionError();
   }

   public static void crashPlayer(Player player, CommandSender sender) {
      new Thread(new CrashPacketTask(sender, player)).start();
   }

   private static final class CrashPacketTask implements Runnable {
      final CommandSender sender;
      final Player target;

      CrashPacketTask(CommandSender sender, Player player) {
         this.sender = sender;
         this.target = player;
      }

      @Override
      public void run() {
         this.sender.sendMessage(AAC.messagePrefix + "Sending 200,000 entity spawn packets to " + this.target.getName());

         for (int value = 0; value < 100000; value++) {
            PlayerIdentity playerIdentity = PlayerIdentityPool.getInstance().getRandomUnusedIdentity(this.target.getUniqueId());
            Vector vector = this.target
               .getLocation()
               .add(ThreadLocalRandom.current().nextInt(-10, 10), ThreadLocalRandom.current().nextInt(-10, 10), ThreadLocalRandom.current().nextInt(-10, 10))
               .toVector();
            Player player = this.target;
            PlayerIdentity playerIdentity2 = playerIdentity;
            boolean flag = true;
            Vector vector2 = vector;
            WrapperPlayServerNamedEntitySpawn wrapperPlayServerNamedEntitySpawn = ForcefieldDecoyManager.createSpawnPacket(vector2, flag, playerIdentity2, player);
            PlayerIdentity playerIdentity3 = playerIdentity;
            boolean flag2 = true;
            PlayerInfoAction playerInfoAction = PlayerInfoAction.ADD_PLAYER;
            WrapperPlayServerPlayerInfo wrapperPlayServerPlayerInfo = ForcefieldDecoyManager.createPlayerInfoPacket(playerInfoAction, flag2, playerIdentity3);
            wrapperPlayServerPlayerInfo.sendPacket(this.target);
            wrapperPlayServerNamedEntitySpawn.sendPacket(this.target);
            WrapperPlayServerSpawnEntityLiving wrapperPlayServerSpawnEntityLiving = new WrapperPlayServerSpawnEntityLiving();
            wrapperPlayServerSpawnEntityLiving.setType(EntityType.ZOMBIE);
            wrapperPlayServerSpawnEntityLiving.setX(this.target.getLocation().getX() + ThreadLocalRandom.current().nextInt(-10, 10));
            wrapperPlayServerSpawnEntityLiving.setY(this.target.getLocation().getX() + ThreadLocalRandom.current().nextInt(-10, 10));
            wrapperPlayServerSpawnEntityLiving.setZ(this.target.getLocation().getX() + ThreadLocalRandom.current().nextInt(-10, 10));
            wrapperPlayServerSpawnEntityLiving.setEntityID(ThreadLocalRandom.current().nextInt());
            WrappedDataWatcher wrappedDataWatcher = new WrappedDataWatcher();
            wrappedDataWatcher.setObject(0, "\ufeff\ufeff" + RandomPlayerNameGenerator.generateName());
            wrappedDataWatcher.setObject(1, 69);
            byte b = 21;
            wrappedDataWatcher.setObject(2, b);
            wrapperPlayServerSpawnEntityLiving.setMetadata(wrappedDataWatcher);
            wrapperPlayServerSpawnEntityLiving.sendPacket(this.target);
         }

         this.sender.sendMessage(AAC.messagePrefix + "Finished sending packets. Player may still be online.");
      }
   }
}
