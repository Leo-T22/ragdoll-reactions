package dev.leo.ragdollreactions.physics;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;

public final class ClientMotionTelemetry {
   private static final int FRESH_TICKS = 3;
   private static final double ACCEL_MATCH_RATIO = 0.7;

   private static final Map<UUID, Sample> SAMPLES = new HashMap<>();

   private ClientMotionTelemetry() {
   }

   public static void update(ServerPlayer player, float horizontalAccelMetersPerSecond, float horizontalSpeedMetersPerSecond) {
      SAMPLES.put(
         player.getUUID(),
         new Sample(player.serverLevel().getGameTime(), horizontalAccelMetersPerSecond, horizontalSpeedMetersPerSecond)
      );
   }

   public static boolean hasRecentAccel(ServerPlayer player, long gameTime, double serverAccelMetersPerSecond) {
      Snapshot snapshot = snapshot(player, gameTime);
      if (!snapshot.fresh()) {
         return false;
      }
      return snapshot.horizontalAccelMetersPerSecond() >= requiredAccel(serverAccelMetersPerSecond);
   }

   public static double requiredAccel(double serverAccelMetersPerSecond) {
      return serverAccelMetersPerSecond * ACCEL_MATCH_RATIO;
   }

   public static Snapshot snapshot(ServerPlayer player, long gameTime) {
      Sample sample = SAMPLES.get(player.getUUID());
      if (sample == null) {
         return new Snapshot(false, Long.MAX_VALUE, 0.0F, 0.0F);
      }

      long ageTicks = gameTime - sample.gameTime();
      return new Snapshot(
         ageTicks <= FRESH_TICKS,
         ageTicks,
         sample.horizontalAccelMetersPerSecond(),
         sample.horizontalSpeedMetersPerSecond()
      );
   }

   public static void clear(UUID playerId) {
      SAMPLES.remove(playerId);
   }

   public static void reset() {
      SAMPLES.clear();
   }

   private record Sample(long gameTime, float horizontalAccelMetersPerSecond, float horizontalSpeedMetersPerSecond) {
   }

   public record Snapshot(boolean fresh, long ageTicks, float horizontalAccelMetersPerSecond, float horizontalSpeedMetersPerSecond) {
   }
}
