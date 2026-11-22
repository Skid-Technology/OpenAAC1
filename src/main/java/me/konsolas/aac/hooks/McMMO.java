package me.konsolas.aac.hooks;

import com.gmail.nossr50.api.AbilityAPI;
import com.gmail.nossr50.events.fake.FakeBlockBreakEvent;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import me.konsolas.aac.AAC;
import me.konsolas.aac.api.HackType;
import me.konsolas.aac.api.PlayerViolationEvent;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class McMMO extends Hook {
   ExpiringMap<UUID, Boolean> fakeBlockBreakExempt = ExpiringMap.builder().expiration(1000L, TimeUnit.SECONDS).expirationPolicy(ExpirationPolicy.ACCESSED).build();

   public McMMO(AAC plugin) {
      super(plugin);
   }

   @EventHandler(priority = EventPriority.LOWEST)
   public void onPlayerViolation(PlayerViolationEvent event) {
      try {
         if (event.getPlayer() == null || !event.getPlayer().isOnline()) {
            return;
         }

         if (this.fakeBlockBreakExempt.containsKey(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            return;
         }

         switch (HackTypeSwitch.SWITCH_TABLE[event.getHackType().ordinal()]) {
            case 1:
            case 2:
            case 3:
               if (AbilityAPI.serratedStrikesEnabled(event.getPlayer()) || AbilityAPI.skullSplitterEnabled(event.getPlayer())) {
                  event.setCancelled(true);
               }

               for (Entity entity : event.getPlayer().getNearbyEntities(7.0, 7.0, 7.0)) {
                  if (entity instanceof LivingEntity && AbilityAPI.isBleeding((LivingEntity)entity)) {
                     event.setCancelled(true);
                     return;
                  }
               }
               break;
            case 4:
               if (AbilityAPI.isAnyAbilityEnabled(event.getPlayer())) {
                  event.setCancelled(true);
               }
               break;
            case 5:
            case 6:
            case 7:
            case 8:
               if (AbilityAPI.berserkEnabled(event.getPlayer())
                  || AbilityAPI.gigaDrillBreakerEnabled(event.getPlayer())
                  || AbilityAPI.superBreakerEnabled(event.getPlayer())
                  || AbilityAPI.treeFellerEnabled(event.getPlayer())) {
                  event.setCancelled(true);
               }
         }
      } catch (Exception ex) {
      }
   }

   @EventHandler(priority = EventPriority.LOWEST)
   public void onFakeBlockBreak(FakeBlockBreakEvent event) {
      this.fakeBlockBreakExempt.put(event.getPlayer().getUniqueId(), false);
      this.fakeBlockBreakExempt.resetExpiration(event.getPlayer().getUniqueId());
   }

   class HackTypeSwitch {
      static final int[] SWITCH_TABLE = new int[HackType.values().length];

      static {
         try {
            SWITCH_TABLE[HackType.ANGLE.ordinal()] = 1;
         } catch (NoSuchFieldError ex) {
         }

         try {
            SWITCH_TABLE[HackType.REACH.ordinal()] = 2;
         } catch (NoSuchFieldError ex2) {
         }

         try {
            SWITCH_TABLE[HackType.KILLAURA.ordinal()] = 3;
         } catch (NoSuchFieldError ex3) {
         }

         try {
            SWITCH_TABLE[HackType.NOSWING.ordinal()] = 4;
         } catch (NoSuchFieldError ex4) {
         }

         try {
            SWITCH_TABLE[HackType.FASTBREAK.ordinal()] = 5;
         } catch (NoSuchFieldError ex5) {
         }

         try {
            SWITCH_TABLE[HackType.NUKER.ordinal()] = 6;
         } catch (NoSuchFieldError ex6) {
         }

         try {
            SWITCH_TABLE[HackType.IMPOSSIBLEINTERACT.ordinal()] = 7;
         } catch (NoSuchFieldError ex7) {
         }

         try {
            SWITCH_TABLE[HackType.PHASE.ordinal()] = 8;
         } catch (NoSuchFieldError ex8) {
         }
      }
   }
}
