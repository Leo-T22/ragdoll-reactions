package dev.leo.ragdollreactions.physics;

import dev.leo.ragdollreactions.config.ReactionSettings;
import dev.leo.ragdollreactions.sound.ReactionSounds;
import dev.leo.sableplayerragdoll.api.RagdollAPI;
import dev.leo.sableplayerragdoll.api.RagdollLaunchOptions;
import dev.leo.sableplayerragdoll.api.RagdollLimbConfig;
import dev.leo.sableplayerragdoll.api.RagdollLimbOptions;
import dev.leo.sableplayerragdoll.api.RagdollSession;
import dev.leo.sableplayerragdoll.block.entity.RagdollPartBlockEntity.BodyPart;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

public final class ReactionLauncher {
   private static final double IMPACT_LIMB_STIFFNESS = 20.0;
   private static final double IMPACT_ARM_ROLL_DEGREES = 40.0;
   private static final double TRIGGER_WAILING_STIFFNESS = 90.0;
   private static final int TRIGGER_WAILING_TICKS = 40;
   private static final RagdollLaunchOptions IMPACT_LAUNCH_OPTIONS = RagdollLaunchOptions.builder().limbs(impactPose()).build();

   private static final Map<UUID, Long> PLAYER_COOLDOWNS = new HashMap<>();

   private ReactionLauncher() {
   }

   public static boolean canTarget(ServerPlayer player, long gameTime) {
      if (player.isDeadOrDying() || player.isSleeping() || player.isPassenger() || player.isSpectator() || player.getAbilities().flying) {
         return false;
      }
      if (player.isCreative() && !ReactionSettings.general().targeting().affectCreative()) {
         return false;
      }
      if (RagdollAPI.isRagdolled(player)) {
         return false;
      }
      return gameTime >= PLAYER_COOLDOWNS.getOrDefault(player.getUUID(), Long.MIN_VALUE);
   }

   public static Vector3d launch(ServerPlayer player, long gameTime, Vector3d velocityMetersPerSecond) {
      Vector3d clamped = clampLinearVelocity(velocityMetersPerSecond);
      Vec3 launchVelocity = new Vec3(clamped.x, clamped.y, clamped.z);
      RagdollSession session = RagdollAPI.launch(player, launchVelocity, IMPACT_LAUNCH_OPTIONS);
      if (session == null) {
         return null;
      }
      session.applyWailing(TRIGGER_WAILING_STIFFNESS, TRIGGER_WAILING_TICKS, 10);

      PLAYER_COOLDOWNS.put(player.getUUID(), gameTime + (long) ReactionSettings.general().launch().cooldownTicks());
      playTriggerSound(player);
      return clamped;
   }

   private static void playTriggerSound(ServerPlayer player) {
      if (!ReactionSettings.general().sound().enabled()) {
         return;
      }
      float pitch = 0.9F + player.serverLevel().getRandom().nextFloat() * 0.2F;
      player.serverLevel().playSound(
         null, player.getX(), player.getY(), player.getZ(),
         ReactionSounds.PUNCH.get(), SoundSource.PLAYERS,
         (float) ReactionSettings.general().sound().volume(), pitch
      );
   }

   public static Vector3d clampLinearVelocity(Vector3d linear) {
      double maxSpeed = ReactionSettings.general().launch().maxSpeed();
      double speed = linear.length();
      if (speed > maxSpeed && speed > 1.0E-6) {
         linear.mul(maxSpeed / speed);
      }

      return linear;
   }

   public static void onPlayerReleased(ServerPlayer player) {
      PLAYER_COOLDOWNS.put(player.getUUID(), player.serverLevel().getGameTime() + (long) ReactionSettings.general().launch().cooldownTicks());
   }

   public static void onPlayerLoggedOut(ServerPlayer player) {
      PLAYER_COOLDOWNS.remove(player.getUUID());
   }

   public static void resetState() {
      PLAYER_COOLDOWNS.clear();
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

   static String fmtVec(Vector3d vec) {
      return fmt(vec.x) + "," + fmt(vec.y) + "," + fmt(vec.z);
   }

   static String fmt(double value) {
      return String.format("%.2f", value);
   }
}
