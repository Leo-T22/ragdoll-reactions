package dev.leo.ragdollreactions.physics;

import dev.leo.ragdollreactions.RagdollReactions;
import dev.leo.ragdollreactions.config.ReactionSettings;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

public final class CrashReactionHandler {
   private static final double MIN_DIRECTION_LENGTH_SQR = 1.0E-6;
   private static final double CRASH_DRIVE_SCALE = 0.25;

   private CrashReactionHandler() {
   }

   public static void onPlayerDamaged(ServerPlayer player, DamageSource source, float originalDamage, float reducedDamage) {
      ReactionSettings.Crash crash = ReactionSettings.triggers().crash();
      if (!ReactionSettings.enabled() || !crash.enabled()) {
         return;
      }
      if (!source.is(DamageTypes.FLY_INTO_WALL)) {
         return;
      }
      if (originalDamage < crash.minDamage()) {
         return;
      }

      long gameTime = player.serverLevel().getGameTime();
      if (!ReactionLauncher.canTarget(player, gameTime)) {
         return;
      }

      Vec3 motion = player.getKnownMovement();
      Vector3d direction = new Vector3d(motion.x, motion.y, motion.z);
      if (direction.lengthSquared() < MIN_DIRECTION_LENGTH_SQR) {
         Vec3 look = player.getLookAngle();
         direction.set(look.x, look.y, look.z);
      }

      if (direction.lengthSquared() < MIN_DIRECTION_LENGTH_SQR) {
         direction.set(0.0, 0.0, 1.0);
      } else {
         direction.normalize();
      }

      double launchSpeed = originalDamage * crash.launchMultiplier() * CRASH_DRIVE_SCALE;
      Vector3d launched = ReactionLauncher.launch(player, gameTime, direction.mul(launchSpeed));
      if (launched == null) {
         return;
      }

      if (ReactionSettings.general().debug().logging()) {
         RagdollReactions.LOGGER.info(
            "[ragdoll_reactions] {} fly-into-wall crash originalDamage={} reducedDamage={} launch={} m/s",
            player.getGameProfile().getName(),
            ReactionLauncher.fmt(originalDamage),
            ReactionLauncher.fmt(reducedDamage),
            ReactionLauncher.fmtVec(launched)
         );
      }
   }
}
