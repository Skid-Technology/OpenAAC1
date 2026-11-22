package me.konsolas.aac.detectors.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import me.konsolas.aac.AAC;
import me.konsolas.aac.api.HackType;
import me.konsolas.aac.detectors.Detector;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public final class Spam extends Detector {
   private ConcurrentMap<UUID, Integer> recentMessageCounts = new ConcurrentHashMap<>();
   private ExpiringMap<UUID, String> lastMessages;
   private ExpiringMap<String, Boolean> flaggedMessages;
   private ExpiringMap<UUID, Boolean> repeatWarnedPlayers;
   private ExpiringMap<UUID, Boolean> chatCooldownPlayers;
   private volatile List<String> whitelist = new ArrayList<>();
   private int messageLimit = 5;
   private int chatSpeed = 10;

   public Spam(AAC plugin) {
      super(plugin);
      this.messageLimit = this.plugin.getConfig().getInt("spam.message_limit");
      this.whitelist = this.plugin.getConfig().getStringList("spam.whitelist");
      this.chatSpeed = this.plugin.getConfig().getInt("spam.chat_speed");
      Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, new ResetMessageCountTask(this), 200L, 200L);
      Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, new DecayViolationsTask(this), 200L, 300L);
      this.lastMessages = ExpiringMap.builder().expirationPolicy(ExpirationPolicy.CREATED).expiration(10L, TimeUnit.SECONDS).build();
      this.flaggedMessages = ExpiringMap.builder().expirationPolicy(ExpirationPolicy.CREATED).expiration(5L, TimeUnit.SECONDS).build();
      this.repeatWarnedPlayers = ExpiringMap.builder().expirationPolicy(ExpirationPolicy.CREATED).expiration(10L, TimeUnit.SECONDS).build();
      this.chatCooldownPlayers = ExpiringMap.builder().expirationPolicy(ExpirationPolicy.CREATED).expiration(this.chatSpeed * 50, TimeUnit.MILLISECONDS).build();
   }

   @EventHandler
   public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
      if (!this.plugin.checkManager.isPlayerExempt(event.getPlayer())) {
         Player player = event.getPlayer();
         if (this.chatCooldownPlayers.containsKey(player.getUniqueId())) {
            if (this.recordViolation(player.getUniqueId(), 5, HackType.SPAM, player.getName() + " failed Spam: tried to chat too quickly")) {
               event.setCancelled(true);
               player.sendMessage(AAC.messagePrefix + this.plugin.messages.getMessage("spam_too_fast"));
            }
         } else {
            if (this.recentMessageCounts.containsKey(player.getUniqueId())) {
               this.recentMessageCounts.put(player.getUniqueId(), this.recentMessageCounts.get(player.getUniqueId()) + 1);
               if (this.recentMessageCounts.get(player.getUniqueId()) == this.messageLimit) {
                  if (this.repeatWarnedPlayers.containsKey(player.getUniqueId())) {
                     if (this.recordViolation(player.getUniqueId(), 6, HackType.SPAM, player.getName() + " failed Spam: sent too many messages")) {
                        player.sendMessage(AAC.messagePrefix + this.plugin.messages.getMessage("spam_slow_chat"));
                     }
                  } else if (this.recordViolation(player.getUniqueId(), 4, HackType.SPAM, player.getName() + " failed Spam: sent too many messages")) {
                     this.repeatWarnedPlayers.put(player.getUniqueId(), true);
                     player.sendMessage(AAC.messagePrefix + this.plugin.messages.getMessage("spam_slow_chat"));
                  }

                  return;
               }
            }

            String str = event.getMessage();

            for (String str2 : this.whitelist) {
               if (isSimilarMessage(str2, str)) {
                  return;
               }
            }

            if (this.lastMessages.containsKey(player.getUniqueId()) && isSimilarMessage(str, this.lastMessages.get(player.getUniqueId()))) {
               if (this.repeatWarnedPlayers.containsKey(player.getUniqueId())) {
                  if (this.recordViolation(player.getUniqueId(), 4, HackType.SPAM, player.getName() + " failed Spam: repeated last message")) {
                     player.sendMessage(AAC.messagePrefix + this.plugin.messages.getMessage("spam_repeat"));
                  }
               } else if (this.recordViolation(player.getUniqueId(), 2, HackType.SPAM, player.getName() + " failed Spam: repeated last message")) {
                  this.repeatWarnedPlayers.put(player.getUniqueId(), true);
                  player.sendMessage(AAC.messagePrefix + this.plugin.messages.getMessage("spam_repeat"));
               }
            } else {
               for (String str3 : this.flaggedMessages.keySet()) {
                  if (isSimilarMessage(str, str3)) {
                     this.flaggedMessages.put(str, true);
                     if (this.repeatWarnedPlayers.containsKey(player.getUniqueId())) {
                        if (this.recordViolation(
                           player.getUniqueId(), 3, HackType.SPAM, player.getName() + " failed Spam: tried to repeat a suspicious message (" + str + ")"
                        )) {
                           player.sendMessage(AAC.messagePrefix + this.plugin.messages.getMessage("spam_repeat_other"));
                           event.setCancelled(true);
                        }
                     } else if (this.recordViolation(
                        player.getUniqueId(), 2, HackType.SPAM, player.getName() + " failed Spam: tried to repeat a suspicious message (" + str + ")"
                     )) {
                        player.sendMessage(AAC.messagePrefix + this.plugin.messages.getMessage("spam_repeat_other"));
                        event.setCancelled(true);
                        this.repeatWarnedPlayers.put(player.getUniqueId(), true);
                     }

                     return;
                  }
               }

               for (String str4 : this.lastMessages.values()) {
                  if (isSimilarMessage(str, str4)) {
                     this.flaggedMessages.put(str, true);
                  }
               }

               this.lastMessages.put(player.getUniqueId(), str);
               this.chatCooldownPlayers.put(player.getUniqueId(), true);
            }
         }
      }
   }

   static ConcurrentMap getRecentMessageCounts(Spam spam) {
      return spam.recentMessageCounts;
   }


   private static boolean isSimilarMessage(String str, String str2) {
      if (str.equalsIgnoreCase(str2)) {
         return true;
      } else if (str.length() >= 3 && str2.length() >= 3) {
         String str3 = str.toLowerCase().replaceAll("[^a-z]", "");
         String str4 = str2.toLowerCase().replaceAll("[^a-z]", "");
         return !str3.isEmpty() && !str4.isEmpty() && str3.equals(str4);
      } else {
         return false;
      }
   }

   private static class ResetMessageCountTask implements Runnable {
      final Spam spam;

      ResetMessageCountTask(Spam spam) {
         this.spam = spam;
      }

      @Override
      public void run() {
         for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            if (!Spam.getRecentMessageCounts(this.spam).containsKey(player.getUniqueId())) {
               Spam.getRecentMessageCounts(this.spam).put(player.getUniqueId(), 0);
            }

            Spam.getRecentMessageCounts(this.spam).put(player.getUniqueId(), 0);
         }
      }
   }

   private static class DecayViolationsTask implements Runnable {
      final Spam spam;

      DecayViolationsTask(Spam spam) {
         this.spam = spam;
      }

      @Override
      public void run() {
         for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            this.spam.decreaseViolationLevel(player.getUniqueId(), 1);
         }
      }
   }
}
