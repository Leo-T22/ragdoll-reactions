package dev.leo.ragdollreactions.physics;

import dev.leo.ragdollreactions.config.ReactionSettings;
import dev.leo.ragdollreactions.sound.ReactionSounds;
import dev.leo.sableplayerragdoll.api.RagdollAPI;
import dev.leo.sableplayerragdoll.mob.api.MobRagdollLaunchOptions;
import dev.leo.sableplayerragdoll.mob.api.MobRagdollSession;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

public final class ReactionMobLauncher {
   private static final double MOB_LAUNCH_SCALE = 0.5;
   private static final double RANDOM_ANGULAR_SPEED = 5.0;
   private static final double SETTLED_SPEED_METERS_PER_SECOND = 0.5;
   private static final int MIN_MOB_RAGDOLL_TICKS = 30;
   private static final int SETTLED_TICKS = 20;
   private static final int MAX_MOB_RAGDOLL_TICKS = 20 * 60 * 10;
   private static final MobRagdollLaunchOptions MOB_LAUNCH_OPTIONS = MobRagdollLaunchOptions.builder()
      .durationTicks(MAX_MOB_RAGDOLL_TICKS)
      .build();
   private static final Map<UUID, Long> MOB_COOLDOWNS = new HashMap<>();
   private static final Map<UUID, ActiveMobLaunch> ACTIVE_MOB_LAUNCHES = new HashMap<>();

   private ReactionMobLauncher() {
   }

   public static boolean canTarget(LivingEntity mob, long gameTime) {
      if (mob.isRemoved() || !mob.isAlive() || mob.isSpectator()) {
         return false;
      }
      if (RagdollAPI.isMobRagdolled(mob)) {
         return false;
      }
      return gameTime >= MOB_COOLDOWNS.getOrDefault(mob.getUUID(), Long.MIN_VALUE);
   }

   public static Vector3d launch(ServerLevel level, LivingEntity mob, long gameTime, Vector3d velocityMetersPerSecond) {
      return launch(level, mob, gameTime, velocityMetersPerSecond, true);
   }

   public static Vector3d launchSilent(ServerLevel level, LivingEntity mob, long gameTime, Vector3d velocityMetersPerSecond) {
      return launch(level, mob, gameTime, velocityMetersPerSecond, false);
   }

   private static Vector3d launch(ServerLevel level, LivingEntity mob, long gameTime, Vector3d velocityMetersPerSecond, boolean playSound) {
      Vector3d clamped = ReactionLauncher.clampLinearVelocity(velocityMetersPerSecond.mul(MOB_LAUNCH_SCALE));
      Vec3 launchVelocity = new Vec3(clamped.x, clamped.y, clamped.z);
      Vec3 angularVelocity = randomAngularVelocity(level.getRandom());
      MobRagdollSession session = RagdollAPI.launchMob(level, mob, launchVelocity, angularVelocity, MOB_LAUNCH_OPTIONS);
      if (session == null) {
         return null;
      }

      ACTIVE_MOB_LAUNCHES.put(mob.getUUID(), new ActiveMobLaunch(session, 0));
      MOB_COOLDOWNS.put(mob.getUUID(), gameTime + (long) ReactionSettings.general().launch().cooldownTicks());
      if (playSound) {
         playTriggerSound(level, mob);
      }
      return clamped;
   }

   public static void onPostPhysicsTick(SubLevelPhysicsSystem physicsSystem, double timeStep) {
      List<MobRagdollSession> settledSessions = new ArrayList<>();
      Iterator<Map.Entry<UUID, ActiveMobLaunch>> iterator = ACTIVE_MOB_LAUNCHES.entrySet().iterator();
      while (iterator.hasNext()) {
         Map.Entry<UUID, ActiveMobLaunch> entry = iterator.next();
         ActiveMobLaunch launch = entry.getValue();
         MobRagdollSession session = launch.session();
         if (session.elapsedTicks() < 0) {
            continue;
         }
         if (session.entity().isRemoved() || !RagdollAPI.isMobRagdolled(session.entity())) {
            iterator.remove();
            continue;
         }

         if (session.elapsedTicks() < MIN_MOB_RAGDOLL_TICKS) {
            if (launch.settledTicks() != 0) {
               entry.setValue(new ActiveMobLaunch(session, 0));
            }
            continue;
         }

         int settledTicks = session.currentVelocity().length() < SETTLED_SPEED_METERS_PER_SECOND
            ? launch.settledTicks() + 1
            : 0;
         if (settledTicks >= SETTLED_TICKS) {
            settledSessions.add(session);
            iterator.remove();
         } else {
            entry.setValue(new ActiveMobLaunch(session, settledTicks));
         }
      }
      for (MobRagdollSession session : settledSessions) {
         session.release();
      }
   }

   private static Vec3 randomAngularVelocity(RandomSource random) {
      Vector3d angular = new Vector3d(
         random.nextDouble() * 2.0 - 1.0,
         random.nextDouble() * 2.0 - 1.0,
         random.nextDouble() * 2.0 - 1.0
      );
      if (angular.lengthSquared() < 1.0E-6) {
         angular.set(0.0, 1.0, 0.0);
      } else {
         angular.normalize();
      }
      angular.mul(RANDOM_ANGULAR_SPEED);
      return new Vec3(angular.x, angular.y, angular.z);
   }

   private static void playTriggerSound(ServerLevel level, LivingEntity mob) {
      if (!ReactionSettings.general().sound().enabled()) {
         return;
      }
      float pitch = 0.9F + level.getRandom().nextFloat() * 0.2F;
      level.playSound(
         null, mob.getX(), mob.getY(), mob.getZ(),
         ReactionSounds.PUNCH.get(), SoundSource.NEUTRAL,
         (float) ReactionSettings.general().sound().volume(), pitch
      );
   }

   public static void onMobReleased(LivingEntity mob, ServerLevel level) {
      ACTIVE_MOB_LAUNCHES.remove(mob.getUUID());
      MOB_COOLDOWNS.put(mob.getUUID(), level.getGameTime() + (long) ReactionSettings.general().launch().cooldownTicks());
   }

   public static void resetState() {
      MOB_COOLDOWNS.clear();
      ACTIVE_MOB_LAUNCHES.clear();
   }

   private record ActiveMobLaunch(MobRagdollSession session, int settledTicks) {
   }
}
