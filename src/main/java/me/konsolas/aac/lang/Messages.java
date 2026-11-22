package me.konsolas.aac.lang;

import java.io.File;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import me.konsolas.aac.AAC;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Messages {
   private AAC plugin;
   private FileConfiguration messagesConfig = null;
   private File messagesFile = null;

   public Messages(AAC plugin) {
      this.plugin = plugin;
      this.saveDefaultConfig();
   }

   public String getMessage(String str) {
      return this.getConfig().getString(str);
   }

   public FileConfiguration getConfig() {
      if (this.messagesConfig == null) {
         this.reloadConfig();
      }

      return this.messagesConfig;
   }

   public void reloadConfig() {
      if (this.messagesFile == null) {
         this.messagesFile = new File(this.plugin.getDataFolder(), "language.yml");
      }

      this.messagesConfig = YamlConfiguration.loadConfiguration(this.messagesFile);
      InputStreamReader inputStreamReader = null;

      try {
         inputStreamReader = new InputStreamReader(this.plugin.getResource("language.yml"), "UTF8");
      } catch (UnsupportedEncodingException ex) {
         ex.printStackTrace();
      }

      if (inputStreamReader != null) {
         YamlConfiguration config = YamlConfiguration.loadConfiguration(inputStreamReader);
         this.messagesConfig.setDefaults(config);
      }
   }

   public void saveDefaultConfig() {
      if (this.messagesFile == null) {
         this.messagesFile = new File(this.plugin.getDataFolder(), "language.yml");
      }

      if (!this.messagesFile.exists()) {
         this.plugin.saveResource("language.yml", false);
      }
   }
}
