package me.konsolas.aac.api;

public enum HackType {
   SPAM("chat.Spam", "spam"),
   COMBATIMPOSSIBLE("combat.CombatImpossible", "combatimpossible"),
   ANGLE("combat.Angle", "angle"),
   CRITICALS("combat.Criticals", "criticals"),
   FASTBOW("combat.Fastbow", "fastbow"),
   FIGHTSPEED("combat.FightSpeed", "fightspeed"),
   FORCEFIELD("combat.Forcefield", "forcefield"),
   KNOCKBACK("combat.Knockback", "knockback"),
   REACH("combat.Reach", "reach"),
   REGEN("combat.Regen", "regen"),
   FASTUSE("combat.FastUse", "fastuse"),
   KILLAURA("combat.KillauraCombined", "killaura"),
   BADPACKETS("movement.BadPackets", "badpackets"),
   GLIDE("movement.Glide", "glide"),
   NORMALMOVEMENTS("movement.NormalMovements", "normalmovements"),
   SPEED("movement.Speed", "speed"),
   PHASE("movement.Phase", "phase"),
   FLY("movement.Fly", "fly"),
   NOFALL("movement.NoFall", "nofall"),
   CLIMB("movement.Climb", "climb"),
   JESUS("movement.Jesus", "jesus"),
   HEADROLL("movement.HeadRoll", "headroll"),
   FASTBREAK("world.FastBreak", "fastbreak"),
   FASTPLACE("world.FastPlace", "fastplace"),
   NUKER("world.Nuker", "nuker"),
   NOSWING("world.NoSwing", "noswing"),
   IMPOSSIBLEINTERACT("world.ImpossibleInteract", "impossibleinteract"),
   PLAYERRADAR("world.PlayerRadar", "playerradar");

   private final String checkPath;
   private final String name;
   private final int id;

   HackType(String str, String str2) {
      this.checkPath = str;
      this.name = str2;
      this.id = this.ordinal();
   }

   @Override
   public String toString() {
      return this.checkPath;
   }

   public String getName() {
      return this.name;
   }

   public int getId() {
      return this.id;
   }
}
