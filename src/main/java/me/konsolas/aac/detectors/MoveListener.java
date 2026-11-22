package me.konsolas.aac.detectors;

import me.konsolas.aac.util.blocks.RelativeBlockCache;
import org.bukkit.event.player.PlayerMoveEvent;

public interface MoveListener {
   void onPlayerMove(PlayerMoveEvent event, boolean flag, boolean flag2, RelativeBlockCache relativeBlockCache, RelativeBlockCache relativeBlockCache2);

   boolean shouldProcessPlayerMove(PlayerMoveEvent event);
}
