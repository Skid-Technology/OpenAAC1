package me.konsolas.aac.api.internal;

import me.konsolas.aac.AAC;
import me.konsolas.aac.api.AACAPI;
import me.konsolas.aac.api.HackType;
import me.konsolas.aac.detectors.movement.Fly;
import me.konsolas.aac.detectors.movement.Phase;
import me.konsolas.aac.detectors.movement.Speed;
import me.konsolas.aac.util.Ping;
import me.konsolas.aac.util.TPS;
import me.konsolas.aac.util.blocks.BlockCollisionChecks;
import org.bukkit.entity.Player;

public final class AACAPIImpl implements AACAPI {
   private AAC plugin;

   public AACAPIImpl(AAC plugin) {
      this.plugin = plugin;
   }

   @Override
   public void reloadAAC() {
      this.plugin.disable();
   }

   @Override
   public void reloadPermissionCache() {
      this.plugin.checkManager.clearBypassCache();
   }

   @Override
   public void invalidateMovementCaches() {
      ((Speed)this.plugin.checkManager.getDetector(HackType.SPEED)).clearCaches();
      ((Fly)this.plugin.checkManager.getDetector(HackType.FLY)).clearCaches();
      ((Phase)this.plugin.checkManager.getDetector(HackType.FLY)).clearCaches();
   }

   @Override
   public void enableCheck(HackType hackType) {
      this.plugin.checkManager.enableCheck(hackType, false);
   }

   @Override
   public void disableCheck(HackType hackType) {
      this.plugin.checkManager.disableCheck(hackType, false);
   }

   @Override
   public boolean isEnabled(HackType hackType) {
      return this.plugin.checkManager.isCheckEnabled(hackType);
   }

   @Override
   public boolean isBypassed(Player player) {
      return this.plugin.checkManager.isPlayerExempt(player);
   }

   @Override
   public double getTPS() {
      return TPS.getTps();
   }

   @Override
   public int getPing(Player player) {
      return Ping.getPing(player);
   }

   @Override
   public int getViolationLevel(Player player, HackType hackType) {
      return this.plugin.checkManager.getDetector(hackType).getViolationLevel(player);
   }

   @Override
   public void setViolationLevel(Player player, HackType hackType, int i) {
      this.plugin.checkManager.getDetector(hackType).setViolationLevel(player, i);
   }

   @Override
   public boolean isPlayerOnGround(Player player) {
      return BlockCollisionChecks.isOnGround(player.getLocation());
   }
}
