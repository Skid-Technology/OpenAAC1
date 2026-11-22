package me.konsolas.aac.components;

import com.comphenix.protocol.PacketType.Play.Client;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import me.konsolas.aac.AAC;
import me.konsolas.aac.gui.admin.HomeGUI;
import me.konsolas.aac.util.CrashUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;

public class AACCommandExecutor implements CommandExecutor, Listener {
   private final AAC plugin;

   public AACCommandExecutor(AAC plugin) {
      this.plugin = plugin;
      plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new RegisterChatListenerTask(this, plugin));
   }

   @EventHandler
   public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
      if (event.getPlayer().hasPermission("AAC.notify") && event.getMessage().startsWith(this.plugin.messages.getMessage("prefix_to_speak"))) {
         event.setCancelled(true);
         this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new StaffChatBroadcastTask(this, event));
      }
   }

   public boolean onCommand(CommandSender sender, Command command, String str, String[] strArray) {
      if (command.getName().equalsIgnoreCase("aac")) {
         return true;
      }

      if (command.getName().equalsIgnoreCase("aacban")) {
         if (strArray.length != 1) {
            sender.sendMessage(AAC.messagePrefix + "Incorrect number of arguments.");
            return false;
         } else {
            String str2 = strArray[0];
            Punisher punisher = this.plugin.punisher;
            if (punisher.isBanned(str2)) {
               StringBuilder sb = new StringBuilder().append(AAC.messagePrefix);
               String str3 = strArray[0];
               StringBuilder sb2 = sb;
               sender.sendMessage(sb2.append(str3).append(" is already banned").toString());
               return false;
            } else if (Bukkit.getPlayer(strArray[0]) == null) {
               StringBuilder sb3 = new StringBuilder().append(AAC.messagePrefix);
               String str4 = strArray[0];
               StringBuilder sb4 = sb3;
               sender.sendMessage(sb4.append(str4).append(" is not online").toString());
               return false;
            } else {
               this.plugin.punisher.banPlayer(Bukkit.getPlayer(strArray[0]));
               Bukkit.getPlayer(strArray[0]).kickPlayer(AAC.messagePrefix + this.plugin.messages.getMessage("ban_message"));
               StringBuilder sb5 = new StringBuilder().append(AAC.messagePrefix);
               String str5 = strArray[0];
               StringBuilder sb6 = sb5;
               sender.sendMessage(sb6.append(str5).append(" has been banned").toString());
               VerboseLogger verboseLogger = this.plugin.verboseLogger;
               sb5 = new StringBuilder();
               String str6 = strArray[0];
               StringBuilder sb7 = sb5;
               verboseLogger.logViolation(sb7.append(str6).append(" was banned by ").append(sender.getName()).toString());
               return true;
            }
         }
      } else if (command.getName().equalsIgnoreCase("aacverbose")) {
         if (!(sender instanceof Player)) {
            sender.sendMessage(AAC.messagePrefix + "You know there's this wonderful thing called a config file... read it");
            return true;
         }

         if (this.plugin.verboseLogger.verboseExclusions.contains(((Player)sender).getUniqueId())) {
            this.plugin.verboseLogger.verboseExclusions.remove(((Player)sender).getUniqueId());
            sender.sendMessage(AAC.messagePrefix + "AAC will now give you verbose output.");
         } else {
            this.plugin.verboseLogger.verboseExclusions.add(((Player)sender).getUniqueId());
            sender.sendMessage(AAC.messagePrefix + "AAC will no longer give you verbose output.");
         }

         return true;
      } else if (command.getName().equalsIgnoreCase("aacunban")) {
         if (strArray.length != 1) {
            sender.sendMessage(AAC.messagePrefix + "Incorrect number of arguments.");
            return false;
         } else {
            String str7 = strArray[0];
            Punisher punisher2 = this.plugin.punisher;
            if (!punisher2.isBanned(str7)) {
               StringBuilder sb8 = new StringBuilder().append(AAC.messagePrefix);
               String str8 = strArray[0];
               StringBuilder sb9 = sb8;
               sender.sendMessage(sb9.append(str8).append(" is not already banned").toString());
               return false;
            } else {
               String str9 = strArray[0];
               Punisher punisher3 = this.plugin.punisher;
               punisher3.unbanPlayer(str9);
               StringBuilder sb10 = new StringBuilder().append(AAC.messagePrefix);
               String str10 = strArray[0];
               StringBuilder sb11 = sb10;
               sender.sendMessage(sb11.append(str10).append(" has been unbanned").toString());
               VerboseLogger verboseLogger2 = this.plugin.verboseLogger;
               sb10 = new StringBuilder();
               String str11 = strArray[0];
               StringBuilder sb12 = sb10;
               verboseLogger2.logViolation(sb12.append(str11).append(" was unbanned by ").append(sender.getName()).toString());
               return true;
            }
         }
      } else if (command.getName().equalsIgnoreCase("aacadmin")) {
         if (!(sender instanceof Player)) {
            sender.sendMessage("You can't see an inventory GUI so...");
            return false;
         } else {
            Player player = (Player)sender;
            new HomeGUI(this.plugin, player);
            return true;
         }
      } else {
         if (command.getName().equalsIgnoreCase("aacreload")) {
            sender.sendMessage(AAC.messagePrefix + "Reload request has been registered.");
            this.plugin.verboseLogger.logViolation("Reload requested by " + sender.getName());
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new ReloadPluginTask(this), 1L);
            return true;
         }

         if (command.getName().equalsIgnoreCase("aackick")) {
            if (strArray.length < 2) {
               sender.sendMessage(AAC.messagePrefix + "Incorrect number of arguments.");
               return false;
            }

            Player player2 = Bukkit.getPlayer(strArray[0]);
            this.plugin.punisher.recordKick(player2);
            if (player2 == null) {
               StringBuilder sb13 = new StringBuilder();
               String str12 = strArray[0];
               StringBuilder sb14 = sb13;
               sender.sendMessage(sb14.append(str12).append(" is not online.").toString());
               return false;
            }

            StringBuilder sb15 = new StringBuilder();

            for (int value = 1; value < strArray.length; value++) {
               String str13 = strArray[value];
               StringBuilder sb16 = sb15;
               sb16.append(str13);
               char c = ' ';
               StringBuilder sb17 = sb15;
               sb17.append(c);
            }

            player2.kickPlayer(AAC.messagePrefix + sb15.toString());
            if (this.plugin.getConfig().getBoolean("aackick_broadcast")) {
               Bukkit.broadcastMessage(AAC.messagePrefix + player2.getName() + this.plugin.messages.getMessage("aac_kick_broadcast"));
            }

            return true;
         } else if (command.getName().equalsIgnoreCase("aacdebug")) {
            if (strArray.length == 0) {
               if (this.plugin.debugPlayer != null) {
                  sender.sendMessage(AAC.messagePrefix + "Ending debug for " + this.plugin.debugPlayer.getName());
                  this.plugin.verboseLogger.stopPlayerDebug();
                  return true;
               } else {
                  sender.sendMessage(AAC.messagePrefix + "No active debug");
                  return false;
               }
            } else if (strArray.length != 1) {
               sender.sendMessage("Incorrect number of arguments");
               return false;
            } else {
               Player player3 = Bukkit.getPlayer(strArray[0]);
               if (player3 == null) {
                  StringBuilder sb18 = new StringBuilder();
                  String str14 = strArray[0];
                  StringBuilder sb19 = sb18;
                  sender.sendMessage(sb19.append(str14).append(" is not online").toString());
                  return false;
               } else {
                  sender.sendMessage("Type /aacdebug without arguments to end the debug session.");
                  sender.sendMessage(AAC.messagePrefix + this.plugin.verboseLogger.startPlayerDebug(player3));
                  return true;
               }
            }
         } else if (command.getName().equalsIgnoreCase("aacmessage")) {
            if (strArray.length < 2) {
               sender.sendMessage(AAC.messagePrefix + "Incorrect number of arguments.");
               return false;
            }

            Player player4 = Bukkit.getPlayer(strArray[0]);
            if (player4 == null) {
               StringBuilder sb20 = new StringBuilder();
               String str15 = strArray[0];
               StringBuilder sb21 = sb20;
               sender.sendMessage(sb21.append(str15).append(" is not online.").toString());
               return false;
            }

            StringBuilder sb22 = new StringBuilder();

            for (int value2 = 1; value2 < strArray.length; value2++) {
               String str16 = strArray[value2];
               StringBuilder sb23 = sb22;
               sb23.append(str16);
               char c2 = ' ';
               StringBuilder sb24 = sb22;
               sb24.append(c2);
            }

            player4.sendMessage(AAC.messagePrefix + sb22.toString());
            return true;
         } else if (!command.getName().equalsIgnoreCase("aacstaffnotify")) {
            if (command.getName().equalsIgnoreCase("aaccrash")) {
               if (strArray.length != 1) {
                  sender.sendMessage(AAC.messagePrefix + "Incorrect number of arguments.");
                  return false;
               } else {
                  Player player5 = Bukkit.getPlayer(strArray[0]);
                  if (player5 == null) {
                     StringBuilder sb25 = new StringBuilder();
                     String str17 = strArray[0];
                     StringBuilder sb26 = sb25;
                     sender.sendMessage(sb26.append(str17).append(" is not online.").toString());
                     return false;
                  } else {
                     CrashUtil.crashPlayer(player5, sender);
                     return true;
                  }
               }
            } else {
               return false;
            }
         } else {
            StringBuilder sb27 = new StringBuilder();

            for (int value3 = 0; value3 < strArray.length; value3++) {
               String str18 = strArray[value3];
               StringBuilder sb28 = sb27;
               sb28.append(str18);
               char c3 = ' ';
               StringBuilder sb29 = sb27;
               sb29.append(c3);
            }

            Bukkit.broadcast(
               ChatColor.translateAlternateColorCodes('&', this.plugin.messages.getMessage("format")).replace("{sender}", sender.getName()) + sb27.toString(), "AAC.notify"
            );
            return true;
         }
      }
   }

   static AAC getPlugin(AACCommandExecutor aacCommandExecutor) {
      return aacCommandExecutor.plugin;
   }

   private static class VersionResponseTask implements Runnable {
      final PacketEvent event;
      final VersionCommandPacketListener listener;

      VersionResponseTask(VersionCommandPacketListener versionCommandPacketListener, PacketEvent event) {
         this.listener = versionCommandPacketListener;
         this.event = event;
      }

      @Override
      public void run() {
         this.event.getPlayer().sendMessage(AAC.messagePrefix + "AAC " + AAC.class.getPackage().getImplementationVersion() + ": Haxor rekker (~konsolas)");
      }
   }

   private static class VersionCommandPacketListener extends PacketAdapter {
      final RegisterChatListenerTask task;

      VersionCommandPacketListener(RegisterChatListenerTask registerChatListenerTask, Plugin plugin, ListenerPriority listenerPriority, PacketType... packetTypeArray) {
         super(plugin, listenerPriority, packetTypeArray);
         this.task = registerChatListenerTask;
      }

      public void onPacketReceiving(PacketEvent event) {
         if (event.getPacket().getType().equals(Client.CHAT)) {
            String str = (String)event.getPacket().getStrings().read(0);
            if (str.equalsIgnoreCase("/aac") || str.startsWith("/aac ")) {
               this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new VersionResponseTask(this, event));
            }
         }
      }
   }

   private static class RegisterChatListenerTask implements Runnable {
      final AAC plugin;
      final AACCommandExecutor executor;

      RegisterChatListenerTask(AACCommandExecutor aacCommandExecutor, AAC plugin) {
         this.executor = aacCommandExecutor;
         this.plugin = plugin;
      }

      @Override
      public void run() {
         ProtocolLibrary.getProtocolManager().addPacketListener(new VersionCommandPacketListener(this, this.plugin, ListenerPriority.LOWEST, Client.CHAT));
      }
   }

   private static class StaffChatBroadcastTask implements Runnable {
      final AsyncPlayerChatEvent event;
      final AACCommandExecutor executor;

      StaffChatBroadcastTask(AACCommandExecutor aacCommandExecutor, AsyncPlayerChatEvent event) {
         this.executor = aacCommandExecutor;
         this.event = event;
      }

      @Override
      public void run() {
         Bukkit.getServer()
            .dispatchCommand(
               this.event.getPlayer(), "aacstaffnotify " + this.event.getMessage().substring(AACCommandExecutor.getPlugin(this.executor).messages.getMessage("prefix_to_speak").length())
            );
      }
   }

   private static class ReloadPluginTask implements Runnable {
      final AACCommandExecutor executor;

      ReloadPluginTask(AACCommandExecutor aacCommandExecutor) {
         this.executor = aacCommandExecutor;
      }

      @Override
      public void run() {
         AACCommandExecutor.getPlugin(this.executor).disable();
      }
   }
}
