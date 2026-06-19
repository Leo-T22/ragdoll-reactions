package dev.leo.ragdollreactions.physics;

import dev.leo.ragdollreactions.RagdollReactions;
import dev.leo.ragdollreactions.config.ReactionSettings;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

public final class MobDamageReactionHandler {
   private static final double MIN_DIRECTION_LENGTH_SQR = 1.0E-6;
   private static final double UPWARD_TILT = 0.35;
   private static final double RAGDOLL_CHANCE = 0.3;

   private MobDamageReactionHandler() {
   }

   public static void onMobDamaged(LivingEntity mob, DamageSource source, float damage) {
      if (!ReactionSettings.enabled() || !ReactionSettings.mobs().enabled()) {
         return;
      }
      ReactionSettings.MobDamage damageTrigger = ReactionSettings.mobs().damage();
      if (!damageTrigger.enabled() || damage < requiredDamageForRemainingHealth(mob, damage)) {
         return;
      }
      if (mob.getRandom().nextDouble() >= RAGDOLL_CHANCE) {
         return;
      }
      if (source.is(DamageTypeTags.IS_FALL) || source.is(DamageTypeTags.IS_EXPLOSION) || source.is(DamageTypeTags.IS_LIGHTNING)) {
         return;
      }

      Vec3 sourcePosition = source.getSourcePosition();
      if (sourcePosition == null) {
         return;
      }
      if (!(mob.level() instanceof ServerLevel level)) {
         return;
      }

      long gameTime = level.getGameTime();
      if (!ReactionMobLauncher.canTarget(mob, gameTime)) {
         return;
      }

      Vec3 away = mob.getBoundingBox().getCenter().subtract(sourcePosition);
      Vector3d direction = new Vector3d(away.x, 0.0, away.z);
      if (direction.lengthSquared() < MIN_DIRECTION_LENGTH_SQR) {
         direction.set(0.0, 1.0, 0.0);
      } else {
         direction.normalize();
         direction.y = UPWARD_TILT;
         direction.normalize();
      }

      double launchSpeed = damage * damageTrigger.launchMultiplier();
      Vector3d launched = ReactionMobLauncher.launch(level, mob, gameTime, direction.mul(launchSpeed));
      if (launched == null) {
         return;
      }

      if (ReactionSettings.general().debug().logging()) {
         RagdollReactions.LOGGER.info(
            "[ragdoll_reactions] mob {} heavy hit damage={} type={} launch={} m/s",
            mob.getType().builtInRegistryHolder().key().location(),
            ReactionLauncher.fmt(damage),
            source.getMsgId(),
            ReactionLauncher.fmtVec(launched)
         );
      }
   }

   private static double requiredDamageForRemainingHealth(LivingEntity mob, float damage) {
      return (mob.getHealth() + damage) * ReactionSettings.mobs().damage().healthFraction();
   }
}
