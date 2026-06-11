package dev.leo.ragdollreactions.physics;

import dev.leo.ragdollreactions.RagdollReactions;
import dev.leo.ragdollreactions.config.ReactionSettings;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.windcharge.WindCharge;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

public final class ExplosionReactionHandler {
   private static final double MIN_EXPLOSION_DIRECTION_LENGTH = 1.0E-4;
   private static final double MIN_FALLOFF = 0.25;

   private ExplosionReactionHandler() {
   }

   public static void onCannonExplosion(ServerLevel level, Vec3 center, double power, double radius) {
      ReactionSettings.CannonExplosions cannonExplosions = ReactionSettings.triggers().cannonExplosions();
      if (!ReactionSettings.enabled() || !cannonExplosions.enabled()) {
         return;
      }
      if (power < cannonExplosions.minPower()) {
         return;
      }

      double effectiveRadius = Math.max(radius, power * 2.0) + cannonExplosions.radiusPadding();
      triggerExplosion(level, center, power, effectiveRadius, cannonExplosions.launchMultiplier(), "cannon");
   }

   public static void onVanillaExplosion(ServerLevel level, Explosion explosion) {
      ReactionSettings.VanillaExplosions vanillaExplosions = ReactionSettings.triggers().vanillaExplosions();
      if (!ReactionSettings.enabled() || !vanillaExplosions.enabled()) {
         return;
      }
      double power = explosion.radius();
      if (power < vanillaExplosions.minPower()) {
         return;
      }

      double effectiveRadius = power * 2.0 + vanillaExplosions.radiusPadding();
      ServerPlayer suppressedSelfWindChargeOwner = suppressedSelfWindChargeOwner(level, explosion);
      triggerExplosion(level, explosion.center(), power, effectiveRadius, vanillaExplosions.launchMultiplier(), "vanilla", suppressedSelfWindChargeOwner);
   }

   private static void triggerExplosion(ServerLevel level, Vec3 center, double power, double effectiveRadius, double launchMultiplier, String kind) {
      triggerExplosion(level, center, power, effectiveRadius, launchMultiplier, kind, null);
   }

   private static void triggerExplosion(
      ServerLevel level,
      Vec3 center,
      double power,
      double effectiveRadius,
      double launchMultiplier,
      String kind,
      ServerPlayer skippedPlayer
   ) {
      double effectiveRadiusSqr = effectiveRadius * effectiveRadius;
      long gameTime = level.getGameTime();
      if (skippedPlayer != null) {
         ReactionSuppressions.suppress(skippedPlayer, gameTime, ReactionSettings.suppressions().selfWindCharge().graceTicks());
      }

      for (ServerPlayer player : level.players()) {
         if (skippedPlayer != null && skippedPlayer.getUUID().equals(player.getUUID())) {
            continue;
         }
         if (!ReactionLauncher.canTarget(player, gameTime)) {
            continue;
         }

         Vec3 playerCenter = player.getBoundingBox().getCenter();
         double distanceSqr = playerCenter.distanceToSqr(center);
         if (distanceSqr > effectiveRadiusSqr) {
            continue;
         }

         Vec3 direction = playerCenter.subtract(center);
         if (direction.lengthSqr() < MIN_EXPLOSION_DIRECTION_LENGTH) {
            direction = new Vec3(0.0, 1.0, 0.0);
         } else {
            direction = direction.normalize();
         }

         double distance = Math.sqrt(distanceSqr);
         double falloff = 1.0 - Math.min(distance / effectiveRadius, 1.0);
         double launchSpeed = power * launchMultiplier * Math.max(MIN_FALLOFF, falloff);
         Vector3d launchVelocity = new Vector3d(direction.x, direction.y, direction.z).mul(launchSpeed);
         Vector3d launched = ReactionLauncher.launch(player, gameTime, launchVelocity);
         if (launched == null) {
            continue;
         }

         if (ReactionSettings.general().debug().logging()) {
            RagdollReactions.LOGGER.info(
               "[ragdoll_reactions] {} {} explosion tumble power={} radius={} launch={} m/s",
               player.getGameProfile().getName(),
               kind,
               ReactionLauncher.fmt(power),
               ReactionLauncher.fmt(effectiveRadius),
               ReactionLauncher.fmtVec(launched)
            );
         }
      }
   }

   private static ServerPlayer suppressedSelfWindChargeOwner(ServerLevel level, Explosion explosion) {
      ReactionSettings.Suppression selfWindCharge = ReactionSettings.suppressions().selfWindCharge();
      if (!selfWindCharge.enabled()) {
         return null;
      }

      Entity directSource = explosion.getDirectSourceEntity();
      LivingEntity indirectSource = explosion.getIndirectSourceEntity();
      if (directSource instanceof WindCharge && indirectSource instanceof ServerPlayer player && player.level() == level) {
         return player;
      }
      return null;
   }
}
