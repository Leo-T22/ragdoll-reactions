package dev.leo.ragdollreactions.physics;

import dev.leo.ragdollreactions.RagdollReactions;
import dev.leo.ragdollreactions.config.ReactionSettings;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

public final class MobExplosionReactionHandler {
   private static final double MIN_EXPLOSION_DIRECTION_LENGTH = 1.0E-4;
   private static final double MIN_FALLOFF = 0.25;

   private MobExplosionReactionHandler() {
   }

   public static void onExplosion(ServerLevel level, Vec3 center, double power) {
      if (!ReactionSettings.enabled() || !ReactionSettings.mobs().enabled()) {
         return;
      }
      ReactionSettings.Explosion explosion = ReactionSettings.mobs().explosion();
      if (!explosion.enabled() || power < explosion.minPower()) {
         return;
      }

      double effectiveRadius = power * 2.0 + explosion.radiusPadding();
      double effectiveRadiusSqr = effectiveRadius * effectiveRadius;
      long gameTime = level.getGameTime();
      AABB searchBox = new AABB(center, center).inflate(effectiveRadius);

      for (Mob mob : level.getEntitiesOfClass(Mob.class, searchBox)) {
         if (!ReactionMobLauncher.canTarget(mob, gameTime)) {
            continue;
         }

         Vec3 mobCenter = mob.getBoundingBox().getCenter();
         double distanceSqr = mobCenter.distanceToSqr(center);
         if (distanceSqr > effectiveRadiusSqr) {
            continue;
         }

         Vec3 direction = mobCenter.subtract(center);
         if (direction.lengthSqr() < MIN_EXPLOSION_DIRECTION_LENGTH) {
            direction = new Vec3(0.0, 1.0, 0.0);
         } else {
            direction = direction.normalize();
         }

         double distance = Math.sqrt(distanceSqr);
         double falloff = 1.0 - Math.min(distance / effectiveRadius, 1.0);
         double launchSpeed = power * explosion.launchMultiplier() * Math.max(MIN_FALLOFF, falloff);
         Vector3d launchVelocity = new Vector3d(direction.x, direction.y, direction.z).mul(launchSpeed);

         Vector3d launched = ReactionMobLauncher.launch(level, mob, gameTime, launchVelocity);
         if (launched == null) {
            continue;
         }

         if (ReactionSettings.general().debug().logging()) {
            RagdollReactions.LOGGER.info(
               "[ragdoll_reactions] mob {} explosion tumble power={} radius={} launch={} m/s",
               mob.getType().builtInRegistryHolder().key().location(),
               ReactionLauncher.fmt(power),
               ReactionLauncher.fmt(effectiveRadius),
               ReactionLauncher.fmtVec(launched)
            );
         }
      }
   }
}
