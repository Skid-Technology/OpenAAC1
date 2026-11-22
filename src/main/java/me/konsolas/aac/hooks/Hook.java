package me.konsolas.aac.hooks;

import me.konsolas.aac.AAC;
import org.bukkit.event.Listener;

public abstract class Hook implements Listener {
   protected AAC plugin;

   public Hook(AAC plugin) {
      this.plugin = plugin;
   }
}
