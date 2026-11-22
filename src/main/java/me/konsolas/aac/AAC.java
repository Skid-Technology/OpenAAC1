package me.konsolas.aac;

import com.comphenix.protocol.ProtocolLibrary;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import me.konsolas.aac.api.AACAPI;
import me.konsolas.aac.components.AACCommandExecutor;
import me.konsolas.aac.components.CombatLogoutListener;
import me.konsolas.aac.components.MovementEventDispatcher;
import me.konsolas.aac.components.PlayerState;
import me.konsolas.aac.components.Punisher;
import me.konsolas.aac.components.TPSManager;
import me.konsolas.aac.components.VerboseLogger;
import me.konsolas.aac.hooks.McMMO;
import me.konsolas.aac.lang.Messages;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class AAC extends JavaPlugin implements Listener {
   public static volatile AACAPI api = null;
   public static final String pluginName = "AAC";
   public static String messagePrefix = "§6[AAC] §r> §6";
   public static Random random = new Random();
   public AAC plugin = this;
   public Player debugPlayer;
   public VerboseLogger verboseLogger;
   public Messages messages;
   public TPSManager tpsManager;
   public PlayerState playerState;
   public MovementEventDispatcher movementDispatcher;
   public CheckManager checkManager;
   public CombatLogoutListener combatLogoutListener;
   public Punisher punisher;
   public AACCommandExecutor commandExecutor;
   SimpleDateFormat logFilenameDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
   SimpleDateFormat logTimestampFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS");
   private FileWriter logFileWriter;
   private PrintWriter logWriter;

   public void onEnable() {

      for (World world : this.getServer().getWorlds()) {
         Entity entity = world.spawnEntity(new Location(world, 0.0, 0.0, 0.0), EntityType.ARMOR_STAND);
         entity.remove();
      }

      if (ProtocolLibrary.getProtocolManager().getMinecraftVersion().getVersion().contains("9")) {
         throw new RuntimeException("THIS BUILD OF AAC IS FOR 1.8.8 ONLY!");
      }

      this.getServer().getPluginManager().registerEvents(this, this);

      this.commandExecutor = new AACCommandExecutor(this);
      this.checkManager = new CheckManager(this);
      this.getServer().getPluginManager().registerEvents(this.commandExecutor, this);
      if (this.getConfig().getBoolean("usetps")) {
         this.tpsManager = new TPSManager(this);
      }

      this.verboseLogger = new VerboseLogger(this);
      this.saveDefaultConfig();
      this.openLog();
      this.reloadConfig();
      this.punisher = new Punisher(this);
      this.registerCommands();
      this.getServer().getScheduler().scheduleSyncDelayedTask(this, new EnableSequence(this), 1L);
      this.movementDispatcher = new MovementEventDispatcher(this);
      this.getServer().getPluginManager().registerEvents(this.movementDispatcher, this);
   }

   @EventHandler
   public void onPlayerLogin(PlayerLoginEvent event) {
      if (api == null) {
         event.disallow(Result.KICK_OTHER, "Server still starting up");
      }
   }

   public void initializeHooks() {
      if (Bukkit.getServer().getPluginManager().getPlugin("mcMMO") != null) {
         Bukkit.getLogger().info("[AAC] Detected mcMMO, setting up hooks...");
         Bukkit.getServer().getPluginManager().registerEvents(new McMMO(this), this);
      }
   }

   public void onDisable() {
      this.playerState.destroy();
      this.punisher.saveBans();
      this.closeLog();
   }

   public void openLog() {
      try {
         if (!this.getConfig().getBoolean("log_file", true)) {
            Bukkit.getLogger().warning("[AAC] Logger functionality has been disabled in the configuration file.");
            return;
         }

         File file = new File(this.getDataFolder(), "logs");
         if (!file.exists()) {
            file.mkdir();
         }

         Date date = new Date();
         File file2 = new File(this.getDataFolder(), "logs/aac_" + this.logFilenameDateFormat.format(date) + ".log");
         if (!file2.exists()) {
            file2.createNewFile();
         }

         this.logFileWriter = new FileWriter(file2, true);
         this.logWriter = new PrintWriter(this.logFileWriter);
      } catch (IOException ex) {
         Bukkit.getLogger().severe("[AAC] Error: Could not setup log file for writing");
         Bukkit.getLogger().severe("[AAC] " + ex.getMessage());
         Bukkit.getLogger().severe("[AAC] Logger functionality has been disabled.");
      }
   }

   private void closeLog() {
      try {
         Bukkit.getLogger().info("[AAC] Saving log file...");
         this.logWriter.close();
      } catch (NullPointerException ex) {
         Bukkit.getLogger().warning("[AAC] Could not save log file as none was created.");
      }
   }

   public void writeLog(String str) {
      if (this.logWriter != null) {
         Date date = new Date();
         this.logWriter.println("[" + this.logTimestampFormat.format(date) + "] " + str);
         this.logWriter.flush();
      }
   }

   public void disable() {
      Bukkit.getLogger().info("[AAC] Beginning disable...");
      Bukkit.getServer().getPluginManager().disablePlugin(this);
      ProtocolLibrary.getProtocolManager().removePacketListeners(this);
      Bukkit.getServer().getPluginManager().enablePlugin(this);
   }

   private void registerCommands() {
      this.getCommand("aac").setExecutor(this.commandExecutor);
      this.getCommand("aacunban").setExecutor(this.commandExecutor);
      this.getCommand("aacban").setExecutor(this.commandExecutor);
      this.getCommand("aacadmin").setExecutor(this.commandExecutor);
      this.getCommand("aackick").setExecutor(this.commandExecutor);
      this.getCommand("aacreload").setExecutor(this.commandExecutor);
      this.getCommand("aacdebug").setExecutor(this.commandExecutor);
      this.getCommand("aacverbose").setExecutor(this.commandExecutor);
      this.getCommand("aacmessage").setExecutor(this.commandExecutor);
      this.getCommand("aacstaffnotify").setExecutor(this.commandExecutor);
      this.getCommand("aaccrash").setExecutor(this.commandExecutor);
   }
}
