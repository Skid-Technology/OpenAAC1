package me.konsolas.aac.api;

import org.bukkit.entity.Player;

public interface AACAPI {
   void reloadAAC();

   void reloadPermissionCache();

   void invalidateMovementCaches();

   void enableCheck(HackType hackType);

   void disableCheck(HackType hackType);

   boolean isEnabled(HackType hackType);

   boolean isBypassed(Player player);

   double getTPS();

   int getPing(Player player);

   int getViolationLevel(Player player, HackType hackType);

   void setViolationLevel(Player player, HackType hackType, int i);

   boolean isPlayerOnGround(Player player);
}
