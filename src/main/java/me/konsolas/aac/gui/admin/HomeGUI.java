package me.konsolas.aac.gui.admin;

import me.konsolas.aac.AAC;
import me.konsolas.aac.gui.GUIBase;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class HomeGUI extends GUIBase {

   public HomeGUI(AAC plugin, Player player) {
      super(player, plugin, 27, "§6AAC: Admin home");
      this.setMenuItem(10, new ItemStack(Material.BED), 1, "Reload AAC", this.splitLore("Use this when you make changes/to the configuration file", "/"));
      this.setMenuItem(16, new ItemStack(Material.REDSTONE_COMPARATOR), 2, "Manage Checks", this.splitLore("Enable or disable checks in /AAC", "/"));
      this.setMenuItem(12, new ItemStack(Material.BOOK), 3, "List possible hackers", this.splitLore("List players in order of their /violations", "/"));
      this.setMenuItem(14, new ItemStack(Material.REDSTONE), 4, "Toggle verbose output", this.splitLore("Toggle verbose output generated /by AAC for yourself", "/"));
      player.openInventory(this.inventory);
   }

   @Override
   protected void onSlotClick(int i) {
      switch (i) {
         case 1:
            this.getViewer().sendMessage(AAC.messagePrefix + "Reload request has been registered.");
            this.plugin.verboseLogger.logViolation("Reload requested by " + this.getViewer().getName());
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new ReloadPluginTask(this), 1L);
            break;
         case 2:
            new CheckGUI(this.plugin, this.getViewer());
            break;
         case 3:
            new PlayerViolationsGUI(this.plugin, this.getViewer());
            break;
         case 4:
            if (this.plugin.verboseLogger.verboseExclusions.contains(this.getViewer().getUniqueId())) {
               this.plugin.verboseLogger.verboseExclusions.remove(this.getViewer().getUniqueId());
               this.getViewer().sendMessage(AAC.messagePrefix + "AAC will now give you verbose output.");
            } else {
               this.plugin.verboseLogger.verboseExclusions.add(this.getViewer().getUniqueId());
               this.getViewer().sendMessage(AAC.messagePrefix + "AAC will no longer give you verbose output.");
            }
      }
   }


   private static class ReloadPluginTask implements Runnable {
      final HomeGUI homeGui;

      ReloadPluginTask(HomeGUI homeGUI) {
         this.homeGui = homeGUI;
      }

      @Override
      public void run() {
         this.homeGui.plugin.disable();
      }
   }
}
