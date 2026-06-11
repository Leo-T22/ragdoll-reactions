package dev.leo.ragdollreactions.physics;

import dev.leo.ragdollreactions.RagdollReactions;
import dev.leo.ragdollreactions.config.ReactionSettings;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

public final class ImpactReactionHandler {
   private static final double BLOCKS_PER_TICK_TO_METERS_PER_SECOND = 20.0;
   private static final int STUMBLE_WINDOW_TICKS = 5;
   private static final long LAG_SKIP_THRESHOLD_NANOS = 100_000_000L;

   private static final Map<UUID, Vec3> LAST_PLAYER_POSITION = new HashMap<>();
   private static final Map<UUID, ArrayDeque<MotionSample>> PLAYER_MOTION_HISTORY = new HashMap<>();
   private static final Map<UUID, Long> LAST_PLAYER_SAMPLE_TICK = new HashMap<>();
   private static final Map<UUID, Long> LAST_PLAYER_SAMPLE_NANOS = new HashMap<>();

   private static boolean loggedFirstPhysicsTick;

   private ImpactReactionHandler() {
   }

   public static void onPostPhysicsTick(SubLevelPhysicsSystem physicsSystem, double timeStep) {
      if (!ReactionSettings.enabled() || !ReactionSettings.triggers().impact().enabled()) {
         return;
      }

      if (!loggedFirstPhysicsTick) {
         loggedFirstPhysicsTick = true;
         RagdollReactions.LOGGER.info("[ragdoll_reactions] accel/decel trigger active (debug={})", ReactionSettings.general().debug().logging());
      }

      ServerLevel level = physicsSystem.getLevel();
      for (ServerPlayer player : level.players()) {
         tryTriggerReaction(level, player);
      }
   }

   private static void tryTriggerReaction(ServerLevel level, ServerPlayer player) {
      UUID playerId = player.getUUID();
      long gameTime = level.getGameTime();

      if (ReactionSuppressions.isTemporarilySuppressed(level, player, gameTime) || isMotionTrackingExcluded(player)) {
         clearMotionTracking(playerId);
         return;
      }

      Vec3 currentPosition = player.position();

      Long lastSampleTick = LAST_PLAYER_SAMPLE_TICK.get(playerId);
      if (lastSampleTick != null && lastSampleTick == gameTime) {
         return;
      }

      LAST_PLAYER_SAMPLE_TICK.put(playerId, gameTime);
      Vec3 previousPosition = LAST_PLAYER_POSITION.put(playerId, currentPosition);
      if (previousPosition == null) {
         return;
      }

      long nowNanos = System.nanoTime();
      Long lastNanos = LAST_PLAYER_SAMPLE_NANOS.put(playerId, nowNanos);
      if (lastNanos != null && nowNanos - lastNanos > LAG_SKIP_THRESHOLD_NANOS) {
         return;
      }

      Vec3 currentBlocksPerTick = sampleHorizontalWorldMotionBlocksPerTick(currentPosition, previousPosition);
      MotionSample previousSample = recordMotionSample(playerId, gameTime, currentBlocksPerTick);
      if (previousSample == null || !ReactionLauncher.canTarget(player, gameTime)) {
         return;
      }

      LaunchSample launchSample = sampleCandidate(previousSample.blocksPerTick(), currentBlocksPerTick);
      if (launchSample == null) {
         return;
      }

      Vector3d linear = composeLaunchLinear(launchSample.previousBlocksPerTick(), launchSample.currentBlocksPerTick(), launchSample.deltaBlocksPerTick());
      Vector3d launched = ReactionLauncher.launch(player, gameTime, linear);
      if (launched == null) {
         return;
      }

      if (ReactionSettings.general().debug().logging()) {
         RagdollReactions.LOGGER.info(
            "[ragdoll_reactions] {} trigger accel={} m/s speed={} m/s launch={} m/s",
            player.getGameProfile().getName(),
            ReactionLauncher.fmt(launchSample.deltaSpeed()),
            ReactionLauncher.fmt(launchSample.currentSpeed()),
            ReactionLauncher.fmtVec(launched)
         );
      }
   }

   private static boolean isMotionTrackingExcluded(ServerPlayer player) {
      return player.isDeadOrDying()
         || player.isSleeping()
         || player.isPassenger()
         || player.isSpectator()
         || player.getAbilities().flying;
   }

   private static Vec3 sampleHorizontalWorldMotionBlocksPerTick(Vec3 currentPosition, Vec3 previousPosition) {
      return new Vec3(currentPosition.x - previousPosition.x, 0.0, currentPosition.z - previousPosition.z);
   }

   private static MotionSample recordMotionSample(UUID playerId, long gameTime, Vec3 blocksPerTick) {
      ArrayDeque<MotionSample> history = PLAYER_MOTION_HISTORY.computeIfAbsent(playerId, unused -> new ArrayDeque<>());
      long oldestAllowedTick = gameTime - STUMBLE_WINDOW_TICKS;
      while (!history.isEmpty() && history.peekFirst().gameTime() < oldestAllowedTick) {
         history.removeFirst();
      }

      MotionSample previousSample = history.peekFirst();
      history.addLast(new MotionSample(gameTime, blocksPerTick));
      return previousSample;
   }

   private static LaunchSample sampleCandidate(Vec3 previousBlocksPerTick, Vec3 currentBlocksPerTick) {
      Vector3d deltaBlocksPerTick = new Vector3d(
         currentBlocksPerTick.x - previousBlocksPerTick.x,
         0.0,
         currentBlocksPerTick.z - previousBlocksPerTick.z
      );
      double deltaSpeed = deltaBlocksPerTick.length() * BLOCKS_PER_TICK_TO_METERS_PER_SECOND;
      ReactionSettings.Impact impact = ReactionSettings.triggers().impact();
      if (deltaSpeed < impact.minVelocityDelta() || deltaSpeed > impact.maxVelocityDelta()) {
         return null;
      }

      double currentSpeed = currentBlocksPerTick.length() * BLOCKS_PER_TICK_TO_METERS_PER_SECOND;
      return new LaunchSample(previousBlocksPerTick, currentBlocksPerTick, deltaBlocksPerTick, deltaSpeed, currentSpeed);
   }

   private static Vector3d composeLaunchLinear(Vec3 previousBlocksPerTick, Vec3 currentBlocksPerTick, Vector3d deltaBlocksPerTick) {
      Vector3d previous = toMetersPerSecond(previousBlocksPerTick);
      Vector3d current = toMetersPerSecond(currentBlocksPerTick);
      Vector3d change = toMetersPerSecond(deltaBlocksPerTick);
      double previousSpeed = previous.lengthSquared();
      double currentSpeed = current.lengthSquared();
      double changeSpeed = change.lengthSquared();

      Vector3d launch;
      if (currentSpeed > previousSpeed) {
         launch = changeSpeed > currentSpeed ? change : current;
      } else if (previousSpeed > 1.0E-6) {
         launch = previous;
      } else {
         launch = change;
      }

      return new Vector3d(launch);
   }

   private static Vector3d toMetersPerSecond(Vec3 blocksPerTick) {
      return new Vector3d(blocksPerTick.x, blocksPerTick.y, blocksPerTick.z).mul(BLOCKS_PER_TICK_TO_METERS_PER_SECOND);
   }

   private static Vector3d toMetersPerSecond(Vector3d blocksPerTick) {
      return new Vector3d(blocksPerTick).mul(BLOCKS_PER_TICK_TO_METERS_PER_SECOND);
   }

   public static void onPlayerDisplaced(ServerPlayer player) {
      clearMotionTracking(player.getUUID());
   }

   public static void onPlayerReleased(ServerPlayer player) {
      clearMotionTracking(player.getUUID());
   }

   public static void onPlayerLoggedOut(ServerPlayer player) {
      clearMotionTracking(player.getUUID());
      ReactionSuppressions.clear(player.getUUID());
   }

   private static void clearMotionTracking(UUID playerId) {
      LAST_PLAYER_POSITION.remove(playerId);
      PLAYER_MOTION_HISTORY.remove(playerId);
      LAST_PLAYER_SAMPLE_TICK.remove(playerId);
      LAST_PLAYER_SAMPLE_NANOS.remove(playerId);
   }

   public static void resetState() {
      LAST_PLAYER_POSITION.clear();
      PLAYER_MOTION_HISTORY.clear();
      LAST_PLAYER_SAMPLE_TICK.clear();
      LAST_PLAYER_SAMPLE_NANOS.clear();
      ReactionSuppressions.reset();
   }

   private record MotionSample(long gameTime, Vec3 blocksPerTick) {
   }

   private record LaunchSample(Vec3 previousBlocksPerTick, Vec3 currentBlocksPerTick, Vector3d deltaBlocksPerTick, double deltaSpeed, double currentSpeed) {
   }
}
