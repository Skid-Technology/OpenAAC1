package me.konsolas.aac.components;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import me.konsolas.aac.AAC;
import me.konsolas.aac.api.HackType;
import me.konsolas.aac.api.PlayerViolationCommandEvent;
import me.konsolas.aac.internal.Destroyable;
import me.konsolas.aac.util.Ping;
import me.konsolas.aac.util.TPS;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

public class Punisher implements Listener, Destroyable {
   public Map<UUID, Integer> kickCounts = new HashMap<>();
   private AAC plugin;
   private BukkitTask scanTask;
   private int banThreshold = 5;
   private int kickTicks = 200;
   private List<UUID> relogBlockedPlayers = new ArrayList<>();
   private Map<String, UUID> bannedPlayers = new HashMap<>();
   private Map<HackType, List<ThresholdCommand>> thresholdCommands = new HashMap<>();
   private FileConfiguration bannedConfig = null;
   private File bannedFile = null;

   public Punisher(AAC plugin) {
      this.plugin = plugin;
      this.banThreshold = this.plugin.getConfig().getInt("ban_threshold", this.banThreshold);
      this.kickTicks = this.plugin.getConfig().getInt("kick_ticks", this.kickTicks);
      this.saveDefaultBannedConfig();

      for (String str : this.getBannedConfig().getStringList("banned")) {
         String[] strArray = str.split(":");
         String str2 = strArray[0];
         String str3 = strArray[1];
         this.bannedPlayers.put(str2, UUID.fromString(str3));
      }

      for (HackType hackType : HackType.values()) {
         ArrayList list = new ArrayList();
         ConfigurationSection section = this.plugin.getConfig().getConfigurationSection(hackType.getName() + ".thresholds");
         if (section == null) {
            this.plugin.getLogger().warning("(Punisher) WARN: Failed to get thresholds for check " + hackType);
         } else {
            for (String str4 : section.getKeys(false)) {
               Object obj = section.get(str4);

               int i;
               try {
                  i = Integer.parseInt(str4);
               } catch (NumberFormatException ex) {
                  this.plugin.getLogger().warning("(Punisher) WARN: Failed to parse integer threshold: " + ex.getMessage());
                  continue;
               }

               if (obj != null && obj instanceof String) {
                  ThresholdCommand thresholdCommand = new ThresholdCommand(this, i, section.getString(str4));
                  list.add(thresholdCommand);
               } else {
                  List<String> list2 = section.getStringList(str4);
                  if (!list2.isEmpty()) {
                     for (String str5 : list2) {
                        ThresholdCommand thresholdCommand2 = new ThresholdCommand(this, i, str5);
                        list.add(thresholdCommand2);
                     }
                  } else {
                     this.plugin.getLogger().warning("(Punisher) WARN: Failed to get multiple commands for check " + hackType + ", threshold " + i);
                  }
               }
            }

            this.thresholdCommands.put(hackType, list);
         }
      }

      this.scanTask = Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(this.plugin, new PunishmentScanTask(this), 20L, 17L);
   }

   public void saveBans() {
      ArrayList list = new ArrayList();

      for (UUID uuid : this.bannedPlayers.values()) {
         list.add(Bukkit.getOfflinePlayer(uuid).getName() + ":" + uuid);
      }

      this.getBannedConfig().set("banned", list);
      this.saveBannedConfig();
   }

   public void recordKick(Player player) {
      if (this.kickCounts.containsKey(player.getUniqueId())) {
         this.kickCounts.put(player.getUniqueId(), this.kickCounts.get(player.getUniqueId()) + 1);
      } else {
         this.kickCounts.put(player.getUniqueId(), 1);
      }

      if (this.kickCounts.get(player.getUniqueId()) > this.banThreshold && this.banThreshold != -1) {
         this.kickCounts.remove(player.getUniqueId());

         for (String str : this.plugin.getConfig().getString("ban_command").split(";")) {
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), this.formatCommand(str, player));
         }
      }
   }

   public void banPlayer(Player player) {
      this.plugin.verboseLogger.logViolation(player.getName() + " has been banned for hacking. (" + player.getUniqueId() + ")");
      this.bannedPlayers.put(player.getName(), player.getUniqueId());
      this.kickCounts.remove(player.getUniqueId());
   }

   private String formatCommand(String str, Player player) {
      str = str.replace("{player}", player.getName());
      str = str.replace("{ping}", Ping.getPing(player) + "");
      return str.replace("{tps}", Math.round(TPS.getTps() * 100.0) / 100.0 + "");
   }

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent event) {
      this.relogBlockedPlayers.add(event.getPlayer().getUniqueId());
      UUID uuid = event.getPlayer().getUniqueId();
      Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new ExpireRelogBlockTask(this, uuid), this.kickTicks);
   }

   @EventHandler
   public void onPlayerLogin(PlayerLoginEvent event) {
      if (!event.getPlayer().hasPermission("AAC.bypass")) {
         if (this.relogBlockedPlayers.contains(event.getPlayer().getUniqueId())) {
            event.disallow(Result.KICK_OTHER, AAC.messagePrefix + this.plugin.messages.getMessage("relog_message"));
         }

         if (!this.kickCounts.containsKey(event.getPlayer().getUniqueId())) {
            this.kickCounts.put(event.getPlayer().getUniqueId(), 0);
         }

         if (this.bannedPlayers.values().contains(event.getPlayer().getUniqueId())) {
            event.disallow(Result.KICK_BANNED, AAC.messagePrefix + this.plugin.messages.getMessage("ban_message"));
         }
      }
   }

   public boolean isBanned(String str) {
      return this.bannedPlayers.containsKey(str);
   }

   public void unbanPlayer(String str) {
      this.kickCounts.put(this.bannedPlayers.get(str), 0);
      this.bannedPlayers.remove(str);
   }

   public FileConfiguration getBannedConfig() {
      if (this.bannedConfig == null) {
         this.reloadBannedConfig();
      }

      return this.bannedConfig;
   }

   public void reloadBannedConfig() {
      if (this.bannedFile == null) {
         this.bannedFile = new File(this.plugin.getDataFolder(), "banned.yml");
      }

      this.bannedConfig = YamlConfiguration.loadConfiguration(this.bannedFile);
      InputStreamReader inputStreamReader = null;

      try {
         inputStreamReader = new InputStreamReader(this.plugin.getResource("banned.yml"), "UTF8");
      } catch (UnsupportedEncodingException ex) {
         ex.printStackTrace();
      }

      if (inputStreamReader != null) {
         YamlConfiguration config = YamlConfiguration.loadConfiguration(inputStreamReader);
         this.bannedConfig.setDefaults(config);
      }
   }

   public void saveDefaultBannedConfig() {
      if (this.bannedFile == null) {
         this.bannedFile = new File(this.plugin.getDataFolder(), "banned.yml");
      }

      if (!this.bannedFile.exists()) {
         this.plugin.saveResource("banned.yml", false);
      }
   }

   public void saveBannedConfig() {
      if (this.bannedConfig != null && this.bannedFile != null) {
         try {
            this.getBannedConfig().save(this.bannedFile);
         } catch (IOException ex) {
            this.plugin.getLogger().log(Level.SEVERE, "Could not save config to " + this.bannedFile, ex);
         }
      }
   }

   @Override
   public void destroy() {
      this.scanTask.cancel();
      this.saveBannedConfig();
   }

   static AAC getPlugin(Punisher punisher) {
      return punisher.plugin;
   }

   static Map<HackType, List<ThresholdCommand>> getThresholdCommands(Punisher punisher) {
      return punisher.thresholdCommands;
   }

   static String formatCommand(Punisher punisher, String str, Player player) {
      return punisher.formatCommand(str, player);
   }

   static List getRelogBlockedPlayers(Punisher punisher) {
      return punisher.relogBlockedPlayers;
   }

   private static class ExecuteThresholdCommandTask implements Runnable {
      final HackType hackType;
      final ThresholdCommand thresholdCommand;
      final Player player;
      final PunishmentScanTask scanTask;

      ExecuteThresholdCommandTask(PunishmentScanTask punishmentScanTask, HackType hackType, ThresholdCommand thresholdCommand, Player player) {
         this.scanTask = punishmentScanTask;
         this.hackType = hackType;
         this.thresholdCommand = thresholdCommand;
         this.player = player;
      }

      @Override
      public void run() {
         Punisher.getPlugin(this.scanTask.punisher)
            .verboseLogger
            .logViolation("(Punisher) ->" + this.hackType.toString() + ": Dispatching command " + Punisher.formatCommand(this.scanTask.punisher, this.thresholdCommand.getCommand(), this.player));
         this.thresholdCommand.executeCommand(this.player, this.hackType);
      }
   }

   private static class PunishmentScanTask implements Runnable {
      final Punisher punisher;

      PunishmentScanTask(Punisher punisher) {
         this.punisher = punisher;
      }

      @Override
      public void run() {
         for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            for (HackType hackType : HackType.values()) {
               if (Punisher.getPlugin(this.punisher).checkManager.isCheckEnabled(hackType)) {
                  int value = Punisher.getPlugin(this.punisher).checkManager.getDetector(hackType).getViolationLevel(player);
                  if (Punisher.getThresholdCommands(this.punisher).get(hackType) != null) {
                     for (ThresholdCommand thresholdCommand : Punisher.getThresholdCommands(this.punisher).get(hackType)) {
                        if (thresholdCommand.crossedThreshold(player, value)) {
                           Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Punisher.getPlugin(this.punisher), new ExecuteThresholdCommandTask(this, hackType, thresholdCommand, player));
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static class ExpireRelogBlockTask implements Runnable {
      final UUID playerId;
      final Punisher punisher;

      ExpireRelogBlockTask(Punisher punisher, UUID uuid) {
         this.punisher = punisher;
         this.playerId = uuid;
      }

      @Override
      public void run() {
         Punisher.getRelogBlockedPlayers(this.punisher).remove(this.playerId);
      }
   }

   private static class ThresholdCommand {
      private final int threshold;
      private final String command;
      private Map<UUID, Boolean> armedByPlayer;
      final Punisher punisher;

      public ThresholdCommand(Punisher punisher, int i, String str) {
         this.punisher = punisher;
         this.armedByPlayer = new HashMap<>();
         this.threshold = i;
         this.command = str;
      }

      public void executeCommand(Player player, HackType hackType) {
         PlayerViolationCommandEvent event = new PlayerViolationCommandEvent(player, hackType, this.command);
         Bukkit.getServer().getPluginManager().callEvent(event);
         if (!event.isCancelled()) {
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), Punisher.formatCommand(this.punisher, event.getCommand(), player));
         }
      }

      public boolean crossedThreshold(Player player, int i) {
         if (!this.armedByPlayer.containsKey(player.getUniqueId())) {
            Map map = this.armedByPlayer;
            UUID uuid = player.getUniqueId();
            boolean flag = true;
            map.put(uuid, flag);
         }

         if (this.armedByPlayer.get(player.getUniqueId())) {
            if (i > this.threshold) {
               Map map2 = this.armedByPlayer;
               UUID uuid2 = player.getUniqueId();
               boolean flag2 = false;
               map2.put(uuid2, flag2);
               return true;
            }
         } else if (i < this.threshold) {
            Map map3 = this.armedByPlayer;
            UUID uuid3 = player.getUniqueId();
            boolean flag3 = true;
            map3.put(uuid3, flag3);
         }

         return false;
      }

      public String getCommand() {
         return this.command;
      }
   }
}
