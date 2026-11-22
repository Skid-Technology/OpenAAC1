package me.konsolas.aac.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import me.konsolas.aac.AAC;
import me.konsolas.aac.internal.Destroyable;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public abstract class GUIBase implements Destroyable, Listener {
   protected AAC plugin;
   protected Inventory inventory;
   private Player viewer;

   public GUIBase(Player player, AAC plugin, int i, String str) {
      this.viewer = player;
      this.plugin = plugin;
      player.closeInventory();
      this.inventory = Bukkit.createInventory(null, i, str);
      Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
   }

   protected void setMenuItem(int i, ItemStack item, int i2, String str, List<String> list) {
      ItemStack item2 = new ItemStack(item);
      ItemMeta itemMeta = item2.getItemMeta();
      itemMeta.setLore(list);
      itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
      itemMeta.addEnchant(Enchantment.DURABILITY, i2, true);
      itemMeta.setDisplayName(str);
      item2.setItemMeta(itemMeta);
      this.inventory.setItem(i, item2);
   }

   protected List<String> splitLore(String str, String str2) {
      ArrayList list = new ArrayList();
      String[] strArray = str.split(str2);
      Collections.addAll(list, strArray);
      return list;
   }

   @EventHandler
   public void onInventoryClick(InventoryClickEvent event) {
      if (event.getInventory().getName().equals(this.inventory.getName())
         && event.getWhoClicked().getUniqueId().equals(this.getViewer().getUniqueId())
         && event.getCurrentItem() != null
         && event.getCurrentItem().getType() != Material.AIR) {
         event.setCancelled(true);
         this.destroy();
         int value = event.getCurrentItem().getEnchantmentLevel(Enchantment.DURABILITY);
         this.onSlotClick(value);
      }
   }

   @Override
   public void destroy() {
      HandlerList.unregisterAll(this);
      this.viewer.closeInventory();
   }

   public Player getViewer() {
      return this.viewer;
   }

   protected abstract void onSlotClick(int i);
}
