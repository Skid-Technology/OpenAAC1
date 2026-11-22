package me.konsolas.aac;

import me.konsolas.aac.api.HackType;
import me.konsolas.aac.api.internal.AACAPIImpl;
import me.konsolas.aac.components.CombatLogoutListener;
import me.konsolas.aac.components.PlayerState;
import me.konsolas.aac.lang.Messages;
import me.konsolas.aac.util.TPS;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class EnableSequence implements Runnable {
   final AAC plugin;

   public EnableSequence(AAC plugin) {
      this.plugin = plugin;
   }

   @Override
   public void run() {
      this.plugin.plugin.getLogger().info("Beginning Enable...");
      this.plugin.initializeHooks();
      Bukkit.getServer().getPluginManager().registerEvents(this.plugin.checkManager, this.plugin.plugin);
      this.plugin.plugin.getLogger().info("Checking config file...");

      for (HackType hackType : HackType.values()) {
         if (this.plugin.getConfig().getBoolean(hackType.getName() + ".enabled")) {
            this.plugin.plugin.getLogger().info("(CheckManager) Enabling check " + hackType);
            this.plugin.checkManager.enableCheck(hackType, false);
         } else {
            this.plugin.plugin.getLogger().info("(CheckManager) Check " + hackType + " is disabled");
         }
      }

      this.plugin.plugin.getLogger().info("Starting API...");
      AAC.api = new AACAPIImpl(this.plugin.plugin);
      this.plugin.combatLogoutListener = new CombatLogoutListener(this.plugin.plugin);
      this.plugin.playerState = new PlayerState(this.plugin.plugin);
      this.plugin.messages = new Messages(this.plugin.plugin);
      Bukkit.getServer().getPluginManager().registerEvents(this.plugin.combatLogoutListener, this.plugin.plugin);
      Bukkit.getServer().getPluginManager().registerEvents(this.plugin.playerState, this.plugin.plugin);
      Bukkit.getServer().getPluginManager().registerEvents(this.plugin.punisher, this.plugin.plugin);
      this.plugin.verboseLogger.logViolation("AAC has been enabled");
      this.plugin.plugin.getLogger().info("AAC has been enabled.");
      AAC.messagePrefix = ChatColor.translateAlternateColorCodes('&', this.plugin.messages.getMessage("prefix"));
      TPS.startMonitoring(this.plugin.plugin);
   }
}
