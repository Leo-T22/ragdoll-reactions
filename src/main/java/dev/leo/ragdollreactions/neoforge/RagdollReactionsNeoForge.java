package dev.leo.ragdollreactions.neoforge;

import dev.leo.ragdollreactions.RagdollReactionsBootstrap;
import dev.leo.ragdollreactions.neoforge.config.ReactionConfig;
import dev.leo.ragdollreactions.physics.CrashReactionHandler;
import dev.leo.ragdollreactions.physics.ExplosionReactionHandler;
import dev.leo.ragdollreactions.physics.FallReactionHandler;
import dev.leo.ragdollreactions.physics.HitReactionHandler;
import dev.leo.ragdollreactions.physics.ImpactReactionHandler;
import dev.leo.ragdollreactions.physics.LightningReactionHandler;
import dev.leo.ragdollreactions.physics.ReactionLauncher;
import dev.leo.ragdollreactions.sound.ReactionSounds;
import dev.leo.sableplayerragdoll.api.RagdollEndEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityStruckByLightningEvent;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Explosion;

@Mod("ragdoll_reactions")
public final class RagdollReactionsNeoForge {
   private static final String CREATE_BIG_CANNONS_PACKAGE = "rbasamoyai.createbigcannons.";

   public RagdollReactionsNeoForge(IEventBus modBus, ModContainer modContainer) {
      modBus.addListener(ReactionConfig::onLoad);
      modBus.addListener(ReactionConfig::onReload);
      ReactionConfig.register(modContainer);
      ReactionSounds.register(modBus);
      modBus.addListener(RagdollReactionsNeoForge::onCommonSetup);
      NeoForge.EVENT_BUS.addListener(RagdollReactionsNeoForge::onRagdollEnd);
      NeoForge.EVENT_BUS.addListener(RagdollReactionsNeoForge::onExplosionDetonate);
      NeoForge.EVENT_BUS.addListener(RagdollReactionsNeoForge::onLivingDamagePre);
      NeoForge.EVENT_BUS.addListener(RagdollReactionsNeoForge::onLivingDamagePost);
      NeoForge.EVENT_BUS.addListener(RagdollReactionsNeoForge::onEntityStruckByLightning);
      NeoForge.EVENT_BUS.addListener(RagdollReactionsNeoForge::onEntityTeleport);
      NeoForge.EVENT_BUS.addListener(RagdollReactionsNeoForge::onPlayerChangedDimension);
      NeoForge.EVENT_BUS.addListener(RagdollReactionsNeoForge::onPlayerRespawn);
      NeoForge.EVENT_BUS.addListener(RagdollReactionsNeoForge::onPlayerLoggedOut);
      NeoForge.EVENT_BUS.addListener(RagdollReactionsNeoForge::onServerStopped);
   }

   private static void onCommonSetup(FMLCommonSetupEvent event) {
      event.enqueueWork(RagdollReactionsBootstrap::init);
   }

   private static void onRagdollEnd(RagdollEndEvent event) {
      ReactionLauncher.onPlayerReleased(event.player());
      ImpactReactionHandler.onPlayerReleased(event.player());
   }

   private static void onExplosionDetonate(ExplosionEvent.Detonate event) {
      if (!(event.getLevel() instanceof ServerLevel level)) {
         return;
      }

      Explosion explosion = event.getExplosion();
      if (isCreateBigCannonsExplosion(explosion)) {
         ExplosionReactionHandler.onCannonExplosion(level, explosion.center(), explosion.radius(), entityRadius(explosion));
      } else {
         ExplosionReactionHandler.onVanillaExplosion(level, explosion);
      }
   }

   private static void onLivingDamagePre(LivingDamageEvent.Pre event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         CrashReactionHandler.onPlayerDamaged(player, event.getSource(), event.getOriginalDamage(), event.getNewDamage());
         FallReactionHandler.onPlayerDamaged(player, event.getSource(), event.getNewDamage());
      }
   }

   private static void onLivingDamagePost(LivingDamageEvent.Post event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         HitReactionHandler.onPlayerDamaged(player, event.getSource(), event.getNewDamage());
      }
   }

   private static void onEntityStruckByLightning(EntityStruckByLightningEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         LightningReactionHandler.onLightningStrike(player);
      }
   }

   private static void onEntityTeleport(EntityTeleportEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         ImpactReactionHandler.onPlayerDisplaced(player);
      }
   }

   private static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         ImpactReactionHandler.onPlayerDisplaced(player);
      }
   }

   private static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         ImpactReactionHandler.onPlayerDisplaced(player);
      }
   }

   private static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         ImpactReactionHandler.onPlayerLoggedOut(player);
         ReactionLauncher.onPlayerLoggedOut(player);
      }
   }

   private static boolean isCreateBigCannonsExplosion(Explosion explosion) {
      return explosion.getClass().getName().startsWith(CREATE_BIG_CANNONS_PACKAGE);
   }

   private static double entityRadius(Explosion explosion) {
      try {
         Object value = explosion.getClass().getMethod("getEntityRadius").invoke(explosion);
         if (value instanceof Number number) {
            return number.doubleValue();
         }
      } catch (ReflectiveOperationException ignored) {
      }
      return explosion.radius();
   }

   private static void onServerStopped(ServerStoppedEvent event) {
      ReactionLauncher.resetState();
      ImpactReactionHandler.resetState();
   }
}
