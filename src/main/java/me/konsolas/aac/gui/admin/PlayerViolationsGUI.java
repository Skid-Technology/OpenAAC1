package me.konsolas.aac.gui.admin;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Map;
import me.konsolas.aac.AAC;
import me.konsolas.aac.api.HackType;
import me.konsolas.aac.gui.GUIBase;
import me.konsolas.aac.util.general.MapUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class PlayerViolationsGUI extends GUIBase {
   public PlayerViolationsGUI(AAC plugin, Player player) {
      super(player, plugin, 54, "§6AAC: Manage player");
      HashMap<String, Integer> map = new HashMap<>();
      int value = 0;

      for (Player player2 : Bukkit.getServer().getOnlinePlayers()) {
         if (++value >= 54) {
            break;
         }

         map.put(player2.getName(), 0);

         for (HackType hackType : HackType.values()) {
            if (this.plugin.checkManager.isCheckEnabled(hackType)) {
               map.put(player2.getName(), (Integer)map.get(player2.getName()) + this.plugin.checkManager.getDetector(hackType).getRecentViolationCount(player2));
            }
         }
      }

      Map<String, Integer> map2 = MapUtil.sortDescendingByValue(map);
      value = 0;

      for (Entry<String, Integer> entry : map2.entrySet()) {
         this.setMenuItem(
            value++, createPlayerSkull(Bukkit.getPlayer((String)entry.getKey()), (String)entry.getKey()), value, (String)entry.getKey(), this.splitLore("VL: " + entry.getValue(), "/")
         );
      }

      player.openInventory(this.inventory);
   }

   private static ItemStack createPlayerSkull(Player player, String str) {
      ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (short)SkullType.PLAYER.ordinal());
      SkullMeta skullMeta = (SkullMeta)item.getItemMeta();
      skullMeta.setOwner(player.getName());
      skullMeta.setDisplayName(str);
      item.setItemMeta(skullMeta);
      return item;
   }

   @Override
   protected void onSlotClick(int i) {
   }
}
