package me.konsolas.aac.util;

import java.util.UUID;

public class PlayerIdentity {
   private UUID uuid;
   private String name;

   public PlayerIdentity(UUID uuid, String str) {
      this.uuid = uuid;
      this.name = str;
   }

   public UUID getUniqueId() {
      return this.uuid;
   }

   public String getName() {
      return this.name;
   }
}
