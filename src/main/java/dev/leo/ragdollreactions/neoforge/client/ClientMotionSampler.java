package dev.leo.ragdollreactions.neoforge.client;

import dev.leo.ragdollreactions.neoforge.network.ClientMotionPacket;
import java.util.ArrayDeque;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ClientTickEvent.Post;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;

public final class ClientMotionSampler {
   private static final double BLOCKS_PER_TICK_TO_METERS_PER_SECOND = 20.0;
   private static final int MOTION_WINDOW_TICKS = 5;
   private static final int KEEP_ALIVE_TICKS = 2;

   private static final ArrayDeque<Vec3> MOTION_HISTORY = new ArrayDeque<>();
   private static Vec3 lastPosition;
   private static int keepAliveTicks;

   private ClientMotionSampler() {
   }

   public static void init() {
      NeoForge.EVENT_BUS.addListener(ClientMotionSampler::onClientTick);
   }

   private static void onClientTick(Post event) {
      Minecraft minecraft = Minecraft.getInstance();
      LocalPlayer player = minecraft.player;
      if (player == null) {
         clearTracking();
         return;
      }

      Vec3 currentPosition = player.position();
      if (lastPosition == null) {
         lastPosition = currentPosition;
         send(0.0F, 0.0F);
         return;
      }

      Vec3 currentMotion = horizontalPositionDelta(currentPosition, lastPosition);
      lastPosition = currentPosition;
      if (MOTION_HISTORY.isEmpty()) {
         MOTION_HISTORY.addLast(currentMotion);
         send(0.0F, (float) horizontalSpeed(currentMotion));
         return;
      }

      while (MOTION_HISTORY.size() > MOTION_WINDOW_TICKS) {
         MOTION_HISTORY.removeFirst();
      }

      double horizontalAccel = maxRecentDeltaSpeed(currentMotion);
      double horizontalSpeed = horizontalSpeed(currentMotion);
      MOTION_HISTORY.addLast(currentMotion);

      if (horizontalAccel > 0.0 || ++keepAliveTicks >= KEEP_ALIVE_TICKS) {
         keepAliveTicks = 0;
         send((float) horizontalAccel, (float) horizontalSpeed);
      }
   }

   private static void clearTracking() {
      MOTION_HISTORY.clear();
      lastPosition = null;
      keepAliveTicks = 0;
   }

   private static Vec3 horizontalPositionDelta(Vec3 currentPosition, Vec3 previousPosition) {
      return new Vec3(currentPosition.x - previousPosition.x, 0.0, currentPosition.z - previousPosition.z);
   }

   private static double horizontalSpeed(Vec3 blocksPerTick) {
      return blocksPerTick.horizontalDistance() * BLOCKS_PER_TICK_TO_METERS_PER_SECOND;
   }

   private static double maxRecentDeltaSpeed(Vec3 currentDeltaMovement) {
      double maxDeltaSpeed = 0.0;
      for (Vec3 previousDeltaMovement : MOTION_HISTORY) {
         maxDeltaSpeed = Math.max(maxDeltaSpeed, horizontalSpeed(currentDeltaMovement.subtract(previousDeltaMovement)));
      }
      return maxDeltaSpeed;
   }

   private static void send(float horizontalAccelMetersPerSecond, float horizontalSpeedMetersPerSecond) {
      PacketDistributor.sendToServer(
         new ClientMotionPacket(horizontalAccelMetersPerSecond, horizontalSpeedMetersPerSecond),
         new CustomPacketPayload[0]
      );
   }
}
