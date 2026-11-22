package me.konsolas.aac.util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import me.konsolas.aac.AAC;
import me.konsolas.aac.util.general.RandomPlayerNameGenerator;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class PlayerIdentityPool {
   private static PlayerIdentityPool instance = new PlayerIdentityPool();
   private List<PlayerIdentity> identities = new ArrayList<>();

   private PlayerIdentityPool() {
      int value = 0;

      for (OfflinePlayer offlinePlayer : Bukkit.getServer().getOfflinePlayers()) {
         this.identities.add(new PlayerIdentity(offlinePlayer.getUniqueId(), offlinePlayer.getName()));
         if (value++ > 500) {
            break;
         }
      }
   }

   private static UUID parseUuid(String str) {
      String str2 = str.replaceFirst("([0-9a-fA-F]{8})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]+)", "$1-$2-$3-$4-$5");
      return UUID.fromString(str2);
   }

   public static PlayerIdentityPool getInstance() {
      return instance;
   }

   public PlayerIdentity getRandomCachedOrGenerated() {
      return this.identities.size() > 0
         ? this.identities.get(AAC.random.nextInt(this.identities.size()))
         : new PlayerIdentity(UUID.randomUUID(), RandomPlayerNameGenerator.generateName());
   }

   public PlayerIdentity getRandomUnusedIdentity(UUID uuid) {
      if (this.identities.size() > 0) {
         boolean flag = true;

         for (int value = 0; value < 5; value++) {
            PlayerIdentity playerIdentity = this.identities.get(AAC.random.nextInt(this.identities.size()));

            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
               if (player.getUniqueId().equals(playerIdentity.getUniqueId())) {
                  flag = false;
               }
            }

            if (flag) {
               return playerIdentity;
            }

            flag = true;
         }
      }

      return new PlayerIdentity(UUID.randomUUID(), RandomPlayerNameGenerator.generateName());
   }

   public PlayerIdentity getRandomOnlineIdentity(UUID uuid) {
      for (int value = 0; value < 5; value++) {
         int value2 = AAC.random.nextInt(Bukkit.getServer().getOnlinePlayers().size());
         Player player = Bukkit.getPlayer(uuid);
         int value3 = 0;

         for (Player player2 : Bukkit.getServer().getOnlinePlayers()) {
            if (value3 == value2) {
               player = player2;
            }

            value3++;
         }

         if (!player.getUniqueId().equals(uuid)) {
            return new PlayerIdentity(player.getUniqueId(), player.getName());
         }
      }

      return null;
   }

   public PlayerIdentity generateRandomIdentity() {
      return new PlayerIdentity(UUID.randomUUID(), RandomPlayerNameGenerator.generateName());
   }
}
