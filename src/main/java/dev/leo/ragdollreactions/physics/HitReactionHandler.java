package dev.leo.ragdollreactions.physics;

import dev.leo.ragdollreactions.RagdollReactions;
import dev.leo.ragdollreactions.config.ReactionSettings;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

public final class HitReactionHandler {
   private static final double MIN_DIRECTION_LENGTH_SQR = 1.0E-6;
   private static final double UPWARD_TILT = 0.35;

   private HitReactionHandler() {
   }

   public static void onPlayerDamaged(ServerPlayer player, DamageSource source, float damage) {
      ReactionSettings.Hit hit = ReactionSettings.triggers().hit();
      if (!ReactionSettings.enabled() || !hit.enabled()) {
         return;
      }
      if (damage < hit.minDamage()) {
         return;
      }

      if (source.is(DamageTypeTags.IS_FALL) || source.is(DamageTypeTags.IS_EXPLOSION) || source.is(DamageTypeTags.IS_LIGHTNING)) {
         return;
      }

      Vec3 sourcePosition = source.getSourcePosition();
      if (sourcePosition == null) {
         return;
      }

      long gameTime = player.serverLevel().getGameTime();
      if (!ReactionLauncher.canTarget(player, gameTime)) {
         return;
      }

      Vec3 away = player.getBoundingBox().getCenter().subtract(sourcePosition);
      Vector3d direction = new Vector3d(away.x, 0.0, away.z);
      if (direction.lengthSquared() < MIN_DIRECTION_LENGTH_SQR) {
         direction.set(0.0, 1.0, 0.0);
      } else {
         direction.normalize();
         direction.y = UPWARD_TILT;
         direction.normalize();
      }

      double launchSpeed = damage * hit.launchMultiplier();
      Vector3d launched = ReactionLauncher.launch(player, gameTime, direction.mul(launchSpeed));
      if (launched == null) {
         return;
      }

      if (ReactionSettings.general().debug().logging()) {
         RagdollReactions.LOGGER.info(
            "[ragdoll_reactions] {} heavy hit damage={} type={} launch={} m/s",
            player.getGameProfile().getName(),
            ReactionLauncher.fmt(damage),
            source.getMsgId(),
            ReactionLauncher.fmtVec(launched)
         );
      }
   }
}
