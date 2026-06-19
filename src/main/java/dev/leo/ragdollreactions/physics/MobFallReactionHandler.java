package dev.leo.ragdollreactions.physics;

import dev.leo.ragdollreactions.RagdollReactions;
import dev.leo.ragdollreactions.config.ReactionSettings;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

public final class MobFallReactionHandler {
   private static final double BLOCKS_PER_TICK_TO_METERS_PER_SECOND = 20.0;
   private static final double GRAVITY_METERS_PER_SECOND_SQUARED = 32.0;

   private MobFallReactionHandler() {
   }

   public static void onMobDamaged(LivingEntity mob, DamageSource source, float fallDamage) {
      if (!ReactionSettings.enabled() || !ReactionSettings.mobs().enabled()) {
         return;
      }
      ReactionSettings.Fall fall = ReactionSettings.mobs().fall();
      if (!fall.enabled() || !source.is(DamageTypes.FALL) || fallDamage < fall.minDamage()) {
         return;
      }
      if (!(mob.level() instanceof ServerLevel level)) {
         return;
      }

      long gameTime = level.getGameTime();
      if (!ReactionMobLauncher.canTarget(mob, gameTime)) {
         return;
      }

      float fallDistance = mob.fallDistance;
      double impactSpeed = Math.sqrt(2.0 * GRAVITY_METERS_PER_SECOND_SQUARED * fallDistance);
      double slamSpeed = impactSpeed * fall.slamMultiplier();

      Vec3 motion = mob.getDeltaMovement();
      Vector3d launchVelocity = new Vector3d(
         motion.x * BLOCKS_PER_TICK_TO_METERS_PER_SECOND,
         -slamSpeed,
         motion.z * BLOCKS_PER_TICK_TO_METERS_PER_SECOND
      );

      Vector3d launched = ReactionMobLauncher.launch(level, mob, gameTime, launchVelocity);
      if (launched == null) {
         return;
      }

      if (ReactionSettings.general().debug().logging()) {
         RagdollReactions.LOGGER.info(
            "[ragdoll_reactions] mob {} hard landing fall={} blocks damage={} launch={} m/s",
            mob.getType().builtInRegistryHolder().key().location(),
            ReactionLauncher.fmt(fallDistance),
            ReactionLauncher.fmt(fallDamage),
            ReactionLauncher.fmtVec(launched)
         );
      }
   }
}
