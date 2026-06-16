package dev.leo.ragdollreactions.neoforge.network;

import dev.leo.ragdollreactions.RagdollReactions;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ReactionNetworking {
   private ReactionNetworking() {
   }

   public static void register(RegisterPayloadHandlersEvent event) {
      PayloadRegistrar registrar = event.registrar(RagdollReactions.MOD_ID);
      registrar.playToServer(ClientMotionPacket.TYPE, ClientMotionPacket.STREAM_CODEC, ClientMotionPacket::handle);
   }
}
