package dev.leo.ragdollreactions.physics;

import dev.leo.ragdollreactions.RagdollReactions;
import dev.leo.ragdollreactions.config.ReactionSettings;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

public final class LightningReactionHandler {
   private static final double BLOCKS_PER_TICK_TO_METERS_PER_SECOND = 20.0;

   private LightningReactionHandler() {
   }

   public static void onLightningStrike(ServerPlayer player) {
      ReactionSettings.Lightning lightning = ReactionSettings.triggers().lightning();
      if (!ReactionSettings.enabled() || !lightning.enabled()) {
         return;
      }

      long gameTime = player.serverLevel().getGameTime();
      if (!ReactionLauncher.canTarget(player, gameTime)) {
         return;
      }

      Vec3 motion = player.getDeltaMovement();
      Vector3d launchVelocity = new Vector3d(
         motion.x * BLOCKS_PER_TICK_TO_METERS_PER_SECOND,
         lightning.launchSpeed(),
         motion.z * BLOCKS_PER_TICK_TO_METERS_PER_SECOND
      );

      Vector3d launched = ReactionLauncher.launch(player, gameTime, launchVelocity);
      if (launched == null) {
         return;
      }

      if (ReactionSettings.general().debug().logging()) {
         RagdollReactions.LOGGER.info(
            "[ragdoll_reactions] {} lightning strike launch={} m/s",
            player.getGameProfile().getName(),
            ReactionLauncher.fmtVec(launched)
         );
      }
   }
}
