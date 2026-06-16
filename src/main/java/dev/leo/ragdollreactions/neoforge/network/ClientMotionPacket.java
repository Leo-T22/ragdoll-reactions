package dev.leo.ragdollreactions.neoforge.network;

import dev.leo.ragdollreactions.RagdollReactions;
import dev.leo.ragdollreactions.physics.ClientMotionTelemetry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientMotionPacket(float horizontalAccelMetersPerSecond, float horizontalSpeedMetersPerSecond) implements CustomPacketPayload {
   public static final Type<ClientMotionPacket> TYPE = new Type<>(
      ResourceLocation.fromNamespaceAndPath(RagdollReactions.MOD_ID, "client_motion")
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, ClientMotionPacket> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.FLOAT, ClientMotionPacket::horizontalAccelMetersPerSecond,
      ByteBufCodecs.FLOAT, ClientMotionPacket::horizontalSpeedMetersPerSecond,
      ClientMotionPacket::new
   );

   @Override
   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(ClientMotionPacket packet, IPayloadContext context) {
      context.enqueueWork(() -> {
         if (context.player() instanceof ServerPlayer player) {
            ClientMotionTelemetry.update(player, packet.horizontalAccelMetersPerSecond(), packet.horizontalSpeedMetersPerSecond());
         }
      });
   }
}
