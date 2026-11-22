package me.konsolas.aac.gui.admin;

import me.konsolas.aac.AAC;
import me.konsolas.aac.api.HackType;
import me.konsolas.aac.gui.GUIBase;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CheckGUI extends GUIBase {
   public CheckGUI(AAC plugin, Player player) {
      super(player, plugin, computeInventorySize(HackType.values().length), "§6AAC: Manage checks");

      for (HackType hackType : HackType.values()) {
         if (this.plugin.checkManager.isCheckEnabled(hackType)) {
            this.setMenuItem(
               hackType.getId(),
               new ItemStack(Material.EMERALD_BLOCK),
               hackType.getId(),
               hackType.toString(),
               this.splitLore("Click to disable the /" + hackType.getName() + " check.", "/")
            );
         } else {
            this.setMenuItem(
               hackType.getId(),
               new ItemStack(Material.REDSTONE_BLOCK),
               hackType.getId(),
               hackType.toString(),
               this.splitLore("Click to enable the /" + hackType.getName() + " check.", "/")
            );
         }
      }

      player.openInventory(this.inventory);
   }

   private static int computeInventorySize(int i) {
      if (i <= 0) {
         return 9;
      }

      int value = (int)Math.ceil(i / 9.0);
      return value > 5 ? 54 : value * 9;
   }

   @Override
   protected void onSlotClick(int i) {
      for (HackType hackType : HackType.values()) {
         if (hackType.getId() == i) {
            if (this.plugin.checkManager.isCheckEnabled(hackType)) {
               this.plugin.checkManager.disableCheck(hackType, false);
               this.plugin.verboseLogger.logViolation("Check " + hackType + " was disabled by " + this.getViewer().getName());
            } else {
               this.plugin.checkManager.enableCheck(hackType, false);
               this.plugin.verboseLogger.logViolation("Check " + hackType + " was enabled by " + this.getViewer().getName());
            }

            return;
         }
      }
   }
}
