package me.konsolas.aac.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import me.konsolas.aac.AAC;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class CombatLogoutListener implements Listener {
   private int combatTicks = 50;
   private boolean killCombatLoggers = true;
   private ExpiringMap<UUID, UUID> lastOpponents;
   private List<UUID> disconnectingPlayers = new ArrayList<>();
   private ExpiringMap<UUID, Boolean> recentlyJoinedPlayers;
   private Map<UUID, DamageCause> lastDamageCauses = new HashMap<>();
   private JavaPlugin plugin;

   public CombatLogoutListener(JavaPlugin javaPlugin) {
      this.plugin = javaPlugin;
      this.combatTicks = this.plugin.getConfig().getInt("combat_time", this.combatTicks);
      this.killCombatLoggers = this.plugin.getConfig().getBoolean("kill_loggers", this.killCombatLoggers);
      this.lastOpponents = ExpiringMap.builder().expirationPolicy(ExpirationPolicy.CREATED).expiration(this.combatTicks * 50, TimeUnit.MILLISECONDS).build();
      this.recentlyJoinedPlayers = ExpiringMap.builder().expirationPolicy(ExpirationPolicy.CREATED).expiration(3000L, TimeUnit.MILLISECONDS).build();
   }

   @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
   public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
      if (event.getDamager().getType() == EntityType.PLAYER) {
         this.lastOpponents.put(event.getDamager().getUniqueId(), event.getEntity().getUniqueId());
         this.lastOpponents.resetExpiration(event.getDamager().getUniqueId());
      }

      if (event.getEntity().getType() == EntityType.PLAYER) {
         this.lastOpponents.put(event.getEntity().getUniqueId(), event.getDamager().getUniqueId());
         this.lastOpponents.resetExpiration(event.getEntity().getUniqueId());
      }
   }

   @EventHandler
   public synchronized void onEntityDamage(EntityDamageEvent event) {
      if (event.getEntity().getType() == EntityType.PLAYER) {
         ExpiringMap expiringMap = this.recentlyJoinedPlayers;
         UUID uuid = event.getEntity().getUniqueId();
         boolean flag = true;
         expiringMap.put(uuid, flag);
         this.recentlyJoinedPlayers.resetExpiration(event.getEntity().getUniqueId());
         this.lastDamageCauses.put(event.getEntity().getUniqueId(), event.getCause());
      }
   }

   public boolean isInCombat(Player player) {
      return this.lastOpponents.containsKey(player.getUniqueId());
   }

   public synchronized boolean hasRecentDamage(Player player) {
      return this.recentlyJoinedPlayers.containsKey(player.getUniqueId());
   }

   public synchronized DamageCause getLastDamageCause(Player player) {
      return this.lastDamageCauses.get(player.getUniqueId());
   }

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent event) {
      if (this.killCombatLoggers && this.lastOpponents.containsKey(event.getPlayer().getUniqueId())) {
         Bukkit.broadcastMessage(AAC.messagePrefix + ((AAC)this.plugin).messages.getMessage("logging_broadcast").replace("{player}", event.getPlayer().getName()));
         event.getPlayer().setHealth(0.0);
         this.disconnectingPlayers.add(event.getPlayer().getUniqueId());
         this.lastOpponents.remove(event.getPlayer().getUniqueId());
      }
   }

   @EventHandler
   public void onPlayerJoin(PlayerJoinEvent event) {
      if (this.killCombatLoggers && this.disconnectingPlayers.contains(event.getPlayer().getUniqueId())) {
         event.getPlayer().sendMessage(AAC.messagePrefix + ((AAC)this.plugin).messages.getMessage("killed_for_logging"));
      }

      this.disconnectingPlayers.remove(event.getPlayer().getUniqueId());
   }
}
