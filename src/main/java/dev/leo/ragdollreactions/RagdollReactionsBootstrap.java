package dev.leo.ragdollreactions;

import dev.leo.ragdollreactions.physics.ImpactReactionHandler;
import dev.leo.ragdollreactions.physics.ReactionMobLauncher;
import dev.ryanhcode.sable.platform.SableEventPlatform;

public final class RagdollReactionsBootstrap {
   private RagdollReactionsBootstrap() {
   }

   public static void init() {
      SableEventPlatform.INSTANCE.onPostPhysicsTick(ImpactReactionHandler::onPostPhysicsTick);
      SableEventPlatform.INSTANCE.onPostPhysicsTick(ReactionMobLauncher::onPostPhysicsTick);
      RagdollReactions.LOGGER.info("[ragdoll_reactions] post-physics accel/decel trigger registered");
   }
}
