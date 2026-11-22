package me.konsolas.aac.components;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import me.konsolas.aac.AAC;
import me.konsolas.aac.util.Ping;
import me.konsolas.aac.util.TPS;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class VerboseLogger {
   public List<UUID> verboseExclusions = new ArrayList<>();
   SimpleDateFormat timestampFormat = new SimpleDateFormat("HH:mm:ss.SSS");
   private AAC plugin;
   private FileWriter debugFileWriter;
   private PrintWriter debugWriter;
   private boolean logToConsole = true;
   private boolean verboseEnabled = false;
   private Thread logWriterThread;
   private Thread debugWriterThread;
   private BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();

   public VerboseLogger(AAC plugin) {
      this.plugin = plugin;
      this.logToConsole = this.plugin.getConfig().getBoolean("log_console", this.logToConsole);
      this.verboseEnabled = this.plugin.getConfig().getBoolean("verbose", this.verboseEnabled);
      this.logWriterThread = new LogWriterThread(this, "AAC-LogWriter");
   }

   public void logViolation(String str) {
      if (this.verboseEnabled) {
         for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            if (player.hasPermission("AAC.verbose") && !this.verboseExclusions.contains(player.getUniqueId())) {
               player.sendMessage(" * §4[AAC] §7" + str);
            }
         }
      }

      if (this.logToConsole && this.verboseEnabled) {
         Bukkit.getConsoleSender().sendMessage(" * §4[AAC] §7" + str);
      }

      this.plugin.writeLog(str);
      if (this.plugin.debugPlayer != null && str.startsWith(this.plugin.debugPlayer.getName())) {
         this.writeDebugLog(str);
      }
   }

   public void logPlayerDebug(Player player, String str) {
      if (this.plugin.debugPlayer != null && this.plugin.debugPlayer.getUniqueId().equals(player.getUniqueId())) {
         this.writeDebugLog(str);
      }
   }

   public String startPlayerDebug(Player player) {
      try {
         this.openDebugLog(player.getName());
      } catch (IOException ex) {
         return "Failed to setup debug log file for writing";
      }

      this.debugWriter.println("AAC debug log for " + player.getName() + " (" + player.getUniqueId() + ")");
      this.debugWriter.println("version: " + AAC.class.getPackage().getImplementationVersion());
      this.debugWriter.println("");
      this.debugWriter.println("- Active configuration");

      try {
         BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(this.plugin.getDataFolder(), "config.yml")));
         Throwable throwable = null;

         while (true) {
            boolean flag = false;

            try {
               flag = true;
               String str;
               if ((str = bufferedReader.readLine()) == null) {
                  flag = false;
                  break;
               }

               if (str.length() > 3 && str.charAt(0) != '#' && str.charAt(3) != '#') {
                  this.debugWriter.println(str);
               }
            } catch (Throwable ex2) {
               throwable = ex2;
               throw ex2;
            } finally {
               if (flag) {
                  if (bufferedReader != null) {
                     if (throwable != null) {
                        try {
                           bufferedReader.close();
                        } catch (Throwable ex3) {
                           Throwable throwable2 = throwable;
                           throwable2.addSuppressed(ex3);
                        }
                     } else {
                        bufferedReader.close();
                     }
                  }
               }
            }
         }

         if (bufferedReader != null) {
            bufferedReader.close();
         }
      } catch (IOException ex4) {
         return "Failed to read config.yml";
      }

      this.debugWriter.println("");
      this.plugin.debugPlayer = player;
      this.debugWriter.println("- Shorthand reference");
      this.debugWriter.println("  - t:threshold");
      this.debugWriter.println("  - e:expected");
      this.debugWriter.println("  - r:recieved");
      this.debugWriter.println("  - m:maximum");
      this.debugWriter.println("  - n:minimum");
      this.debugWriter.println("  - u:using as");
      this.debugWriter.println("  - f:flags");
      this.debugWriter.println("  - !:not");
      this.debugWriter.println("");
      this.writeDebugLog("--- BEGIN DEBUG DATA ---");
      return "Now saving data for " + player.getName();
   }

   public void stopPlayerDebug() {
      if (this.plugin.debugPlayer != null) {
         this.writeDebugLog("--- END DEBUG DATA ---");
         this.plugin.debugPlayer = null;
         this.closeDebugLog();
      }
   }

   private void openDebugLog(String str) throws IOException {
      try {
         File file = this.plugin.getDataFolder();
         if (!file.exists()) {
            file.mkdir();
         }

         File file2 = new File(file, str + ".debug.log");
         if (!file2.exists()) {
            file2.createNewFile();
         } else {
            file2.delete();
            file2.createNewFile();
         }

         this.debugFileWriter = new FileWriter(file2, true);
         this.debugWriter = new PrintWriter(this.debugFileWriter);
      } catch (IOException ex) {
         throw ex;
      }
   }

   private void closeDebugLog() {
      try {
         this.logWriterThread.interrupt();
         Bukkit.getLogger().info("[AAC] Saving log file...");
         this.debugWriter.close();
      } catch (NullPointerException ex) {
         Bukkit.getLogger().warning("[AAC] Could not save log file as none was created.");
      }
   }

   public void writeDebugLog(String str) {
      if (this.debugWriter != null) {
         Date date = new Date();
         this.debugWriter
            .println(
               "at "
                  + this.timestampFormat.format(date)
                  + ": "
                  + str
                  + " [ping:"
                  + Ping.getPing(this.plugin.debugPlayer)
                  + " tps:"
                  + Math.round(TPS.getTps() * 1000.0) / 1000.0
                  + "]"
            );
         this.debugWriter.flush();
      }
   }

   static BlockingQueue getMessageQueue(VerboseLogger verboseLogger) {
      return verboseLogger.messageQueue;
   }

   static AAC getPlugin(VerboseLogger verboseLogger) {
      return verboseLogger.plugin;
   }

   private static class LogWriterThread extends Thread {
      final VerboseLogger logger;

      LogWriterThread(VerboseLogger verboseLogger, String str) {
         super(str);
         this.logger = verboseLogger;
      }

      @Override
      public void run() {
         try {
            while (true) {
               String str = (String)VerboseLogger.getMessageQueue(this.logger).take();
               VerboseLogger.getPlugin(this.logger).writeLog(str);
            }
         } catch (InterruptedException ex) {
            VerboseLogger.getPlugin(this.logger).getLogger().info("Thread AAC-LogWriter exiting");
         }
      }
   }
}
