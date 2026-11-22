package me.konsolas.aac.detectors.movement;

import com.comphenix.protocol.PacketType.Play.Client;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import me.konsolas.aac.AAC;
import me.konsolas.aac.api.HackType;
import me.konsolas.aac.detectors.Detector;
import me.konsolas.aac.detectors.MoveListener;
import me.konsolas.aac.util.blocks.BlockCollisionChecks;
import me.konsolas.aac.util.blocks.RelativeBlockCache;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.Plugin;

public final class NoFall extends Detector implements MoveListener {
   private double requiredVelocity = -0.2;
   private boolean dealDamage = true;
   private Map<UUID, Double> groundHeights = new HashMap<>();
   private Map<UUID, Double> spoofedFallStartY = new HashMap<>();
   private Set<UUID> exemptGroundDamage = new HashSet<>();
   private Queue<FallSnapshot> pendingFallSnapshots = new ConcurrentLinkedQueue<>();

   public NoFall(AAC plugin) {
      super(plugin);
      this.requiredVelocity = this.plugin.getConfig().getDouble("nofall.required_velocity", this.requiredVelocity);
      this.dealDamage = this.plugin.getConfig().getBoolean("nofall.deal_damage", this.dealDamage);
      GroundPacketListener groundPacketListener = new GroundPacketListener(this, this.plugin, Client.POSITION, Client.POSITION_LOOK, Client.FLYING);
      this.packetListeners.add(groundPacketListener);
      ProtocolLibrary.getProtocolManager().addPacketListener(groundPacketListener);
   }

   @Override
   public boolean shouldProcessPlayerMove(PlayerMoveEvent event) {
      return event.getPlayer().getGameMode() != GameMode.CREATIVE
         && event.getPlayer().getGameMode() != GameMode.SPECTATOR
         && !event.getPlayer().getAllowFlight()
         && event.getPlayer().getVehicle() == null;
   }

   @Override
   public void setCurrentTick(long ticks) {
      while (!this.pendingFallSnapshots.isEmpty()) {
         FallSnapshot fallSnapshot = this.pendingFallSnapshots.poll();
         boolean flag = BlockCollisionChecks.isOnGround(FallSnapshot.getLocation(fallSnapshot));
         boolean flag2 = FallSnapshot.isOnGround(fallSnapshot);
         double d = FallSnapshot.getDeltaY(fallSnapshot);
         double y = FallSnapshot.getY(fallSnapshot);
         this.plugin.verboseLogger.logPlayerDebug(FallSnapshot.getPlayer(fallSnapshot), "(NoFall) Packet [onGround r:" + flag2 + "]");
         if (!flag && this.groundHeights.containsKey(FallSnapshot.getPlayer(fallSnapshot).getUniqueId())) {
            double d2 = this.groundHeights.get(FallSnapshot.getPlayer(fallSnapshot).getUniqueId());
            if (d2 - y > 3.0) {
               if (d < this.requiredVelocity) {
                  if (flag2
                     && this.recordViolation(
                        FallSnapshot.getPlayer(fallSnapshot).getUniqueId(),
                        1,
                        HackType.NOFALL,
                        FallSnapshot.getPlayer(fallSnapshot).getName() + " failed NoFall: Claimed to be on the ground midair"
                     )) {
                     this.spoofedFallStartY.put(FallSnapshot.getPlayer(fallSnapshot).getUniqueId(), d2);
                  }
               } else if (flag2) {
                  this.spoofedFallStartY.put(FallSnapshot.getPlayer(fallSnapshot).getUniqueId(), d2);
               }
            }
         }
      }
   }

   @Override
   public void onPlayerMove(PlayerMoveEvent event, boolean flag, boolean flag2, RelativeBlockCache relativeBlockCache, RelativeBlockCache relativeBlockCache2) {
      double y = event.getTo().getY() - event.getFrom().getY();
      double y2 = event.getTo().getY();
      if (flag2 && y >= 0.0 && this.spoofedFallStartY.containsKey(event.getPlayer().getUniqueId())) {
         this.plugin.verboseLogger.logPlayerDebug(event.getPlayer(), "(NoFall) " + event.getPlayer().getName() + " landed");
         float f = this.calculateFallDamage(this.spoofedFallStartY.get(event.getPlayer().getUniqueId()), y2);
         if (f >= 1.0) {
            if (this.dealDamage) {
               this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new DeferredFallSetbackTask(this, event, f), 2L);
               this.spoofedFallStartY.remove(event.getPlayer().getUniqueId());
               this.groundHeights.remove(event.getPlayer().getUniqueId());
            } else {
               this.spoofedFallStartY.remove(event.getPlayer().getUniqueId());
               this.groundHeights.remove(event.getPlayer().getUniqueId());
            }
         }
      }

      if (flag2 && y == 0.0) {
         this.exemptGroundDamage.add(event.getPlayer().getUniqueId());
      } else {
         this.exemptGroundDamage.remove(event.getPlayer().getUniqueId());
      }

      if (!flag && !flag2) {
         if (this.groundHeights.containsKey(event.getPlayer().getUniqueId())) {
            if (y2 > this.groundHeights.get(event.getPlayer().getUniqueId())) {
               this.groundHeights.put(event.getPlayer().getUniqueId(), y2);
               this.plugin
                  .verboseLogger
                  .logPlayerDebug(
                     event.getPlayer(), "(NoFall) " + event.getPlayer().getName() + " New highestY for " + event.getPlayer().getName() + " [u:" + y2 + "]"
                  );
            }
         } else {
            this.groundHeights.put(event.getPlayer().getUniqueId(), y2);
            this.plugin
               .verboseLogger
               .logPlayerDebug(
                  event.getPlayer(), "(NoFall) " + event.getPlayer().getName() + " created highestY for " + event.getPlayer().getName() + " [u:" + y2 + "]"
               );
         }
      } else {
         this.groundHeights.remove(event.getPlayer().getUniqueId());
      }
   }

   @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
   public void onBlockBreak(BlockBreakEvent event) {
      this.groundHeights.put(event.getPlayer().getUniqueId(), event.getPlayer().getLocation().getY());
      this.spoofedFallStartY.remove(event.getPlayer().getUniqueId());
   }

   @EventHandler
   public void onPlayerRespawn(PlayerRespawnEvent event) {
      this.groundHeights.remove(event.getPlayer().getUniqueId());
      this.spoofedFallStartY.remove(event.getPlayer().getUniqueId());
   }

   private float calculateFallDamage(double d, double d2) {
      return (float)(d - d2 - 3.0);
   }

   public boolean hasSpoofedFall(Player player) {
      return this.spoofedFallStartY.containsKey(player.getUniqueId());
   }

   static Queue getPendingFallSnapshots(NoFall noFall) {
      return noFall.pendingFallSnapshots;
   }

   static Set getExemptGroundDamage(NoFall noFall) {
      return noFall.exemptGroundDamage;
   }


   private static class GroundPacketListener extends PacketAdapter {
      final NoFall noFall;

      GroundPacketListener(NoFall noFall, Plugin plugin, PacketType... packetTypeArray) {
         super(plugin, packetTypeArray);
         this.noFall = noFall;
      }

      public void onPacketReceiving(PacketEvent event) {
         if (event.getPlayer().getGameMode() != GameMode.CREATIVE
            && event.getPlayer().getGameMode() != GameMode.SPECTATOR
            && !event.getPlayer().getAllowFlight()
            && event.getPlayer().getVehicle() == null) {
            PacketContainer packet = event.getPacket();
            Location location = event.getPlayer().getLocation();
            if (event.getPacketType() == Client.POSITION_LOOK || event.getPacketType() == Client.POSITION) {
               location = new Location(
                  event.getPlayer().getWorld(), (Double)packet.getDoubles().read(0), (Double)packet.getDoubles().read(1), (Double)packet.getDoubles().read(2)
               );
            }

            NoFall.getPendingFallSnapshots(this.noFall)
               .add(
                  new FallSnapshot(
                     this.noFall,
                     location,
                     (Boolean)packet.getBooleans().read(0),
                     event.getPlayer().getLocation().getY(),
                     event.getPlayer().getVelocity().getY(),
                     event.getPlayer()
                  )
               );
            if (NoFall.getExemptGroundDamage(this.noFall).contains(event.getPlayer().getUniqueId())) {
               packet.getBooleans().write(0, true);
            }
         }
      }
   }

   private static class ApplyFallDamageTask implements Runnable {
      final PacketContainer packet;
      final DeferredFallSetbackTask setbackTask;

      ApplyFallDamageTask(DeferredFallSetbackTask deferredFallSetbackTask, PacketContainer packet) {
         this.setbackTask = deferredFallSetbackTask;
         this.packet = packet;
      }

      @Override
      public void run() {
         try {
            ProtocolLibrary.getProtocolManager().recieveClientPacket(this.setbackTask.event.getPlayer(), this.packet, false);
         } catch (Exception ex) {
            this.setbackTask.noFall.getPlugin().verboseLogger.logViolation("(NoFall) Failed to deal fall damage to player: " + ex.getMessage());
         }
      }
   }

   private static class DeferredFallSetbackTask implements Runnable {
      final PlayerMoveEvent event;
      final float fallDamage;
      final NoFall noFall;

      DeferredFallSetbackTask(NoFall noFall, PlayerMoveEvent event, float f) {
         this.noFall = noFall;
         this.event = event;
         this.fallDamage = f;
      }

      @Override
      public void run() {
         this.event.getPlayer().setFallDistance(this.fallDamage + 3.0F);
         PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(Client.FLYING);
         packet.getBooleans().write(0, true);
         Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.noFall.getPlugin(), new ApplyFallDamageTask(this, packet), 1L);
         this.noFall.getPlugin().verboseLogger.logViolation("(NoFall) Dealing " + this.fallDamage + " damage to " + this.event.getPlayer().getName());
      }
   }

   private static class FallSnapshot {
      private Location location;
      private boolean onGround;
      private double y;
      private double deltaY;
      private Player player;
      final NoFall noFall;

      public FallSnapshot(NoFall noFall, Location location, boolean flag, double d, double d2, Player player) {
         this.noFall = noFall;
         this.location = location;
         this.onGround = flag;
         this.y = d;
         this.deltaY = d2;
         this.player = player;
      }

      static Location getLocation(FallSnapshot fallSnapshot) {
         return fallSnapshot.location;
      }

      static boolean isOnGround(FallSnapshot fallSnapshot) {
         return fallSnapshot.onGround;
      }

      static double getDeltaY(FallSnapshot fallSnapshot) {
         return fallSnapshot.deltaY;
      }

      static double getY(FallSnapshot fallSnapshot) {
         return fallSnapshot.y;
      }

      static Player getPlayer(FallSnapshot fallSnapshot) {
         return fallSnapshot.player;
      }
   }
}
