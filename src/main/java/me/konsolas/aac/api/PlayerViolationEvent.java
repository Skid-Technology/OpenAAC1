package me.konsolas.aac.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public final class PlayerViolationEvent extends PlayerEvent implements Cancellable {
   private static final HandlerList HANDLERS = new HandlerList();
   private boolean cancelled;
   private HackType hackType;
   private int violations;
   private String message;

   public PlayerViolationEvent(Player player, HackType hackType, int i, String str) {
      super(player);
      this.hackType = hackType;
      this.violations = i;
      this.message = str;
   }

   public static HandlerList getHandlerList() {
      return HANDLERS;
   }

   public int getViolations() {
      return this.violations;
   }

   public HackType getHackType() {
      return this.hackType;
   }

   public String getMessage() {
      return this.message;
   }

   public boolean isCancelled() {
      return this.cancelled;
   }

   public void setCancelled(boolean flag) {
      this.cancelled = flag;
   }

   public HandlerList getHandlers() {
      return HANDLERS;
   }
}
