package me.konsolas.aac.heuristics.base;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import me.konsolas.aac.AAC;
import me.konsolas.aac.internal.Destroyable;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public abstract class Heuristic implements Listener, Destroyable {
   protected AAC plugin;
   private Map<UUID, Integer> scores = new HashMap<>();

   public Heuristic(AAC plugin) {
      this.plugin = plugin;
      Bukkit.getServer().getPluginManager().registerEvents(this, this.plugin);

      for (Player player : Bukkit.getServer().getOnlinePlayers()) {
         this.scores.put(player.getUniqueId(), 0);
      }
   }

   public void setHeuristicScore(Player player, int i) {
      this.scores.put(player.getUniqueId(), i > 0 ? i : 0);
   }

   public int getHeuristicScore(Player player) {
      try {
         this.updateScore(player);
         return this.scores.get(player.getUniqueId());
      } catch (NullPointerException ex) {
         return 0;
      }
   }

   public abstract int getDetectionChance(Player player);

   protected void updateScore(Player player) {
   }

   @Override
   public void destroy() {
      HandlerList.unregisterAll(this);
   }

   @EventHandler(priority = EventPriority.MONITOR)
   public void onPlayerJoin(PlayerJoinEvent event) {
      this.scores.put(event.getPlayer().getUniqueId(), 0);
   }

   @EventHandler(priority = EventPriority.MONITOR)
   public void onPlayerQuit(PlayerQuitEvent event) {
      this.scores.remove(event.getPlayer().getUniqueId());
   }

   @EventHandler(priority = EventPriority.MONITOR)
   public void onPlayerKick(PlayerKickEvent event) {
      this.scores.remove(event.getPlayer().getUniqueId());
   }
}
