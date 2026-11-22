package me.konsolas.aac.api;

import me.konsolas.aac.AAC;

public final class AACAPIProvider {
   private AACAPIProvider() {
      throw new AssertionError();
   }

   public static AACAPI getAPI() {
      if (AAC.api == null) {
         throw new APINotLoadedException();
      } else {
         return AAC.api;
      }
   }

   public static boolean isAPILoaded() {
      return AAC.api != null;
   }
}
