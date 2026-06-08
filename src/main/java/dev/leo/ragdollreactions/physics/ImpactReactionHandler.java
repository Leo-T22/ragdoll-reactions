package dev.leo.ragdollreactions.physics;

import dev.leo.ragdollreactions.RagdollReactions;
import dev.leo.ragdollreactions.config.ReactionSettings;
import dev.leo.sableplayerragdoll.api.RagdollAPI;
import dev.leo.sableplayerragdoll.api.RagdollLaunchOptions;
import dev.leo.sableplayerragdoll.api.RagdollLimbConfig;
import dev.leo.sableplayerragdoll.api.RagdollLimbOptions;
import dev.leo.sableplayerragdoll.block.entity.RagdollPartBlockEntity.BodyPart;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.companion.math.BoundingBox3d;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public final class ImpactReactionHandler {
   private static final double BLOCKS_PER_TICK_TO_METERS_PER_SECOND = 20.0;
   private static final double IMPACT_LIMB_STIFFNESS = 20.0;
   private static final double IMPACT_ARM_ROLL_DEGREES = 100.0;
   private static final int STUMBLE_WINDOW_TICKS = 5;
   private static final long LAG_SKIP_THRESHOLD_NANOS = 100_000_000L;
   private static final double MIN_EXPLOSION_DIRECTION_LENGTH = 1.0E-4;
   private static final RagdollLaunchOptions IMPACT_LAUNCH_OPTIONS = RagdollLaunchOptions.builder().limbs(impactPose()).build();

   private static final Map<UUID, Long> PLAYER_COOLDOWNS = new HashMap<>();
   private static final Map<UUID, Vec3> LAST_PLAYER_POSITION = new HashMap<>();
   private static final Map<UUID, ArrayDeque<MotionSample>> PLAYER_MOTION_HISTORY = new HashMap<>();
   private static final Map<UUID, Long> LAST_PLAYER_SAMPLE_TICK = new HashMap<>();
   private static final Map<UUID, Long> LAST_PLAYER_SAMPLE_NANOS = new HashMap<>();
   private static final Map<UUID, Long> LAST_SUBLEVEL_NEAR_TICK = new HashMap<>();

   private static boolean loggedFirstPhysicsTick;

   private ImpactReactionHandler() {
   }

   public static void onPostPhysicsTick(SubLevelPhysicsSystem physicsSystem, double timeStep) {
      if (!ReactionSettings.enabled()) {
         return;
      }

      if (!loggedFirstPhysicsTick) {
         loggedFirstPhysicsTick = true;
         RagdollReactions.LOGGER.info("[ragdoll_reactions] sublevel impact trigger active (debug={})", ReactionSettings.debugLogging());
      }

      ServerLevel level = physicsSystem.getLevel();
      for (ServerPlayer player : level.players()) {
         tryTriggerReaction(level, player);
      }
   }

   private static void tryTriggerReaction(ServerLevel level, ServerPlayer player) {
      Vec3 currentPosition = player.position();
      UUID playerId = player.getUUID();
      long gameTime = level.getGameTime();

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

      updateSubLevelProximity(level, player, gameTime);

      if (!hasRecentSubLevelContext(playerId, gameTime)) {
         return;
      }

      Vec3 currentBlocksPerTick = sampleHorizontalWorldMotionBlocksPerTick(currentPosition, previousPosition);
      MotionSample previousSample = recordMotionSample(playerId, gameTime, currentBlocksPerTick);
      if (previousSample == null || !canTarget(player, gameTime)) {
         return;
      }

      if (RagdollAPI.isRagdolled(player)) {
         return;
      }

      LaunchSample launchSample = sampleCandidate(previousSample.blocksPerTick(), currentBlocksPerTick);
      if (launchSample == null) {
         return;
      }

      Vector3d linear = composeLaunchLinear(launchSample.previousBlocksPerTick(), launchSample.currentBlocksPerTick(), launchSample.deltaBlocksPerTick());
      Vec3 launchVelocity = new Vec3(linear.x, linear.y, linear.z);

      if (RagdollAPI.launch(player, launchVelocity, IMPACT_LAUNCH_OPTIONS) == null) {
         return;
      }

      PLAYER_COOLDOWNS.put(playerId, gameTime + (long) ReactionSettings.cooldownTicks());
      if (ReactionSettings.debugLogging()) {
         RagdollReactions.LOGGER.info(
            "[ragdoll_reactions] {} sublevel hit delta={} m/s speed={} m/s launch={} m/s",
            player.getGameProfile().getName(),
            fmt(launchSample.deltaSpeed()),
            fmt(launchSample.currentSpeed()),
            fmtVec3dc(linear)
         );
      }
   }

   public static void onCannonExplosion(ServerLevel level, Vec3 center, double power, double radius) {
      if (!ReactionSettings.enabled() || !ReactionSettings.cannonExplosionsEnabled()) {
         return;
      }
      if (power < ReactionSettings.minCannonExplosionPower()) {
         return;
      }

      double effectiveRadius = Math.max(radius, power * 2.0) + ReactionSettings.cannonExplosionRadiusPadding();
      double effectiveRadiusSqr = effectiveRadius * effectiveRadius;
      long gameTime = level.getGameTime();
      for (ServerPlayer player : level.players()) {
         if (!canTarget(player, gameTime) || RagdollAPI.isRagdolled(player)) {
            continue;
         }

         Vec3 playerCenter = player.getBoundingBox().getCenter();
         double distanceSqr = playerCenter.distanceToSqr(center);
         if (distanceSqr > effectiveRadiusSqr) {
            continue;
         }

         Vec3 direction = playerCenter.subtract(center);
         if (direction.lengthSqr() < MIN_EXPLOSION_DIRECTION_LENGTH) {
            direction = new Vec3(0.0, 1.0, 0.0);
         } else {
            direction = direction.normalize();
         }

         double distance = Math.sqrt(distanceSqr);
         double falloff = 1.0 - Math.min(distance / effectiveRadius, 1.0);
         double launchSpeed = power * ReactionSettings.cannonExplosionLaunchMultiplier() * Math.max(0.25, falloff);
         Vector3d clampedLaunch = clampLinearVelocity(toJoml(direction.scale(launchSpeed)));
         Vec3 launchVelocity = new Vec3(clampedLaunch.x, clampedLaunch.y, clampedLaunch.z);
         if (RagdollAPI.launch(player, launchVelocity, IMPACT_LAUNCH_OPTIONS) == null) {
            continue;
         }

         PLAYER_COOLDOWNS.put(player.getUUID(), gameTime + (long) ReactionSettings.cooldownTicks());
         if (ReactionSettings.debugLogging()) {
            RagdollReactions.LOGGER.info(
               "[ragdoll_reactions] {} cannon explosion tumble power={} radius={} launch={} m/s",
               player.getGameProfile().getName(),
               fmt(power),
               fmt(effectiveRadius),
               fmtVec3dc(clampedLaunch)
            );
         }
      }
   }

   private static void updateSubLevelProximity(ServerLevel level, ServerPlayer player, long gameTime) {
      double threshold = ReactionSettings.minSubLevelSpeed();
      AABB searchAABB = player.getBoundingBox().inflate(1.0);
      BoundingBox3d searchBounds = new BoundingBox3d(searchAABB);
      Vector3d velDest = new Vector3d();
      Vector3d playerPos = new Vector3d(player.getX(), player.getY(), player.getZ());
      for (SubLevel subLevel : Sable.HELPER.getAllIntersecting(level, searchBounds)) {
         if (Sable.HELPER.getVelocity(level, subLevel, playerPos, velDest).length() >= threshold) {
            LAST_SUBLEVEL_NEAR_TICK.put(player.getUUID(), gameTime);
            return;
         }
      }
   }

   private static boolean hasRecentSubLevelContext(UUID playerId, long gameTime) {
      Long lastTick = LAST_SUBLEVEL_NEAR_TICK.get(playerId);
      return lastTick != null && gameTime - lastTick <= STUMBLE_WINDOW_TICKS;
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
      if (deltaSpeed < ReactionSettings.minVelocityDelta()) {
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

      return clampLinearVelocity(new Vector3d(launch));
   }

   private static Vector3d toMetersPerSecond(Vec3 blocksPerTick) {
      return new Vector3d(blocksPerTick.x, blocksPerTick.y, blocksPerTick.z).mul(BLOCKS_PER_TICK_TO_METERS_PER_SECOND);
   }

   private static Vector3d toMetersPerSecond(Vector3d blocksPerTick) {
      return new Vector3d(blocksPerTick).mul(BLOCKS_PER_TICK_TO_METERS_PER_SECOND);
   }

   private static Vector3d toJoml(Vec3 vec) {
      return new Vector3d(vec.x, vec.y, vec.z);
   }

   private static Vector3d clampLinearVelocity(Vector3d linear) {
      double maxSpeed = ReactionSettings.maxLaunchSpeed();
      double speed = linear.length();
      if (speed > maxSpeed && speed > 1.0E-6) {
         linear.mul(maxSpeed / speed);
      }

      return linear;
   }

   private static RagdollLimbOptions impactPose() {
      return RagdollLimbOptions.builder()
         .limb(BodyPart.HEAD, RagdollLimbConfig.builder().stiffness(IMPACT_LIMB_STIFFNESS))
         .limb(BodyPart.TORSO, RagdollLimbConfig.builder().stiffness(IMPACT_LIMB_STIFFNESS))
         .limb(BodyPart.LEFT_ARM, RagdollLimbConfig.builder().stiffness(IMPACT_LIMB_STIFFNESS).roll(IMPACT_ARM_ROLL_DEGREES))
         .limb(BodyPart.RIGHT_ARM, RagdollLimbConfig.builder().stiffness(IMPACT_LIMB_STIFFNESS).roll(-IMPACT_ARM_ROLL_DEGREES))
         .limb(BodyPart.LEFT_LEG, RagdollLimbConfig.builder().stiffness(IMPACT_LIMB_STIFFNESS))
         .limb(BodyPart.RIGHT_LEG, RagdollLimbConfig.builder().stiffness(IMPACT_LIMB_STIFFNESS))
         .build();
   }

   private static boolean canTarget(ServerPlayer player, long gameTime) {
      if (player.isDeadOrDying() || player.isSleeping() || player.isPassenger() || player.isSpectator() || player.isFallFlying() || player.getAbilities().flying) {
         return false;
      }
      if (player.isCreative() && !ReactionSettings.affectCreative()) {
         return false;
      }
      return gameTime >= PLAYER_COOLDOWNS.getOrDefault(player.getUUID(), Long.MIN_VALUE);
   }

   public static void onPlayerReleased(ServerPlayer player) {
      UUID playerId = player.getUUID();
      LAST_PLAYER_POSITION.remove(playerId);
      PLAYER_MOTION_HISTORY.remove(playerId);
      LAST_PLAYER_SAMPLE_TICK.remove(playerId);
      LAST_PLAYER_SAMPLE_NANOS.remove(playerId);
      LAST_SUBLEVEL_NEAR_TICK.remove(playerId);
      PLAYER_COOLDOWNS.put(playerId, player.serverLevel().getGameTime() + (long) ReactionSettings.cooldownTicks());
   }

   public static void resetState() {
      PLAYER_COOLDOWNS.clear();
      LAST_PLAYER_POSITION.clear();
      PLAYER_MOTION_HISTORY.clear();
      LAST_PLAYER_SAMPLE_TICK.clear();
      LAST_PLAYER_SAMPLE_NANOS.clear();
      LAST_SUBLEVEL_NEAR_TICK.clear();
   }

   private static String fmtVec3dc(Vector3dc vec) {
      return fmt(vec.x()) + "," + fmt(vec.y()) + "," + fmt(vec.z());
   }

   private static String fmt(double value) {
      return String.format("%.2f", value);
   }

   private record MotionSample(long gameTime, Vec3 blocksPerTick) {
   }

   private record LaunchSample(Vec3 previousBlocksPerTick, Vec3 currentBlocksPerTick, Vector3d deltaBlocksPerTick, double deltaSpeed, double currentSpeed) {
   }
}
