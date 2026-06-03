package dev.leo.ragdollreactions;

import dev.leo.ragdollreactions.physics.ImpactReactionHandler;
import dev.ryanhcode.sable.platform.SableEventPlatform;

public final class RagdollReactionsBootstrap {
   private RagdollReactionsBootstrap() {
   }

   public static void init() {
      SableEventPlatform.INSTANCE.onPostPhysicsTick(ImpactReactionHandler::onPostPhysicsTick);
      RagdollReactions.LOGGER.info("[ragdoll_reactions] post-physics accel/decel trigger registered");
   }
}
