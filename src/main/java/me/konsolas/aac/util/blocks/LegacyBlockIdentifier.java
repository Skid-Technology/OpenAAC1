package me.konsolas.aac.util.blocks;

public class LegacyBlockIdentifier {
   private int hasteAmplifier;
   private int fatigueAmplifier;

   public LegacyBlockIdentifier(int i, int i2) {
      this.hasteAmplifier = i;
      this.fatigueAmplifier = i2;
   }

   public int getHasteAmplifier() {
      return this.hasteAmplifier;
   }

   public int getFatigueAmplifier() {
      return this.fatigueAmplifier;
   }
}
