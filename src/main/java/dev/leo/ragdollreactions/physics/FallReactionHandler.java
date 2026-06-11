package dev.leo.ragdollreactions.physics;

import dev.leo.ragdollreactions.RagdollReactions;
import dev.leo.ragdollreactions.config.ReactionSettings;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

public final class FallReactionHandler {
   private static final double BLOCKS_PER_TICK_TO_METERS_PER_SECOND = 20.0;
   private static final double GRAVITY_METERS_PER_SECOND_SQUARED = 32.0;

   private FallReactionHandler() {
   }

   public static void onHardLanding(ServerPlayer player, float fallDistance) {
      ReactionSettings.Fall fall = ReactionSettings.triggers().fall();
      if (!ReactionSettings.enabled() || !fall.enabled()) {
         return;
      }
      if (fallDistance < fall.minDistance()) {
         return;
      }

      long gameTime = player.serverLevel().getGameTime();
      if (!ReactionLauncher.canTarget(player, gameTime)) {
         return;
      }

      double impactSpeed = Math.sqrt(2.0 * GRAVITY_METERS_PER_SECOND_SQUARED * fallDistance);
      double slamSpeed = impactSpeed * fall.slamMultiplier();

      Vec3 motion = player.getDeltaMovement();
      Vector3d launchVelocity = new Vector3d(
         motion.x * BLOCKS_PER_TICK_TO_METERS_PER_SECOND,
         -slamSpeed,
         motion.z * BLOCKS_PER_TICK_TO_METERS_PER_SECOND
      );

      Vector3d launched = ReactionLauncher.launch(player, gameTime, launchVelocity);
      if (launched == null) {
         return;
      }

      if (ReactionSettings.general().debug().logging()) {
         RagdollReactions.LOGGER.info(
            "[ragdoll_reactions] {} hard landing fall={} blocks impact={} m/s launch={} m/s",
            player.getGameProfile().getName(),
            ReactionLauncher.fmt(fallDistance),
            ReactionLauncher.fmt(impactSpeed),
            ReactionLauncher.fmtVec(launched)
         );
      }
   }
}
