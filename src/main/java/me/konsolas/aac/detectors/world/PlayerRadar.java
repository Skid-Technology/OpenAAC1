package me.konsolas.aac.detectors.world;

import me.konsolas.aac.AAC;
import me.konsolas.aac.detectors.Detector;
import me.konsolas.aac.detectors.MoveListener;
import me.konsolas.aac.util.blocks.BlockUtil;
import me.konsolas.aac.util.blocks.RelativeBlockCache;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

public final class PlayerRadar extends Detector implements MoveListener {
   private double requiredDist = 15.0;
   private double maxDist = 48.0;

   public PlayerRadar(AAC plugin) {
      super(plugin);
      this.requiredDist = this.plugin.getConfig().getDouble("playerradar.required_dist");
      this.maxDist = this.plugin.getConfig().getDouble("playerradar.max_dist");
   }

   @Override
   public boolean shouldProcessPlayerMove(PlayerMoveEvent event) {
      return event.getTo().getX() != event.getFrom().getX() || event.getTo().getY() != event.getFrom().getY() || event.getTo().getZ() != event.getFrom().getZ();
   }

   @Override
   public void onPlayerMove(PlayerMoveEvent event, boolean flag, boolean flag2, RelativeBlockCache relativeBlockCache, RelativeBlockCache relativeBlockCache2) {
      for (Player player : Bukkit.getServer().getOnlinePlayers()) {
         if (player.getWorld().equals(event.getPlayer().getWorld()) && !player.equals(event.getPlayer())) {
            Player player2 = event.getPlayer();
            if (!(player2.getLocation().distanceSquared(player.getLocation()) < this.requiredDist * this.requiredDist)
               && !(player2.getLocation().distanceSquared(player.getLocation()) > this.maxDist * this.maxDist)) {
               Vector vector = player.getLocation().toVector();
               Vector vector2 = player.getLocation().add(0.0, 1.9, 0.0).toVector();
               Vector vector3 = player2.getLocation().toVector();
               Vector vector4 = player2.getLocation().add(0.0, 1.9, 0.0).toVector();
               int value = (int)vector3.distance(vector);
               Vector vector5 = vector3.clone().subtract(vector).normalize();
               Vector vector6 = vector4.clone().subtract(vector).normalize();
               Vector vector7 = vector3.clone().subtract(vector2).normalize();
               Vector vector8 = vector4.clone().subtract(vector2).normalize();
               Vector vector9 = vector.clone();
               boolean flag3 = true;

               for (int value2 = 0; value2 < value; value2++) {
                  Block block = player2.getWorld().getBlockAt(vector9.toLocation(player2.getWorld()));
                  if (!BlockUtil.isTransparent(block.getType())) {
                     flag3 = false;
                     break;
                  }

                  vector9.add(vector5);
               }

               vector9 = vector.clone();
               if (!flag3) {
                  flag3 = true;

                  for (int value3 = 0; value3 < value; value3++) {
                     Block block2 = player2.getWorld().getBlockAt(vector9.toLocation(player2.getWorld()));
                     if (!BlockUtil.isTransparent(block2.getType())) {
                        if (player.canSee(player2)) {
                           player.hidePlayer(player2);
                        }

                        if (player2.canSee(player)) {
                           player2.hidePlayer(player);
                        }

                        flag3 = false;
                        break;
                     }

                     vector9.add(vector6);
                  }
               }

               vector9 = vector2.clone();
               if (!flag3) {
                  flag3 = true;

                  for (int value4 = 0; value4 < value; value4++) {
                     Block block3 = player2.getWorld().getBlockAt(vector9.toLocation(player2.getWorld()));
                     if (!BlockUtil.isTransparent(block3.getType())) {
                        if (player.canSee(player2)) {
                           player.hidePlayer(player2);
                        }

                        if (player2.canSee(player)) {
                           player2.hidePlayer(player);
                        }

                        flag3 = false;
                        break;
                     }

                     vector9.add(vector7);
                  }
               }

               vector9 = vector2.clone();
               if (!flag3) {
                  flag3 = true;

                  for (int value5 = 0; value5 < value; value5++) {
                     Block block4 = player2.getWorld().getBlockAt(vector9.toLocation(player2.getWorld()));
                     if (!BlockUtil.isTransparent(block4.getType())) {
                        if (player.canSee(player2)) {
                           player.hidePlayer(player2);
                        }

                        if (player2.canSee(player)) {
                           player2.hidePlayer(player);
                        }

                        flag3 = false;
                        break;
                     }

                     vector9.add(vector8);
                  }
               }

               if (flag3 && !player.canSee(player2)) {
                  player.showPlayer(player2);
               }

               if (flag3 && !player2.canSee(player)) {
                  player2.showPlayer(player);
               }
            } else {
               if (!player.canSee(player2)) {
                  player.showPlayer(player2);
               }

               if (!player2.canSee(player)) {
                  player2.showPlayer(player);
               }
            }
         }
      }
   }

   @Override
   public void destroy() {
      super.destroy();

      for (Player player : Bukkit.getServer().getOnlinePlayers()) {
         for (Player player2 : Bukkit.getServer().getOnlinePlayers()) {
            player.showPlayer(player2);
         }
      }
   }
}
