package dev.leo.ragdollreactions.config;

public final class ReactionSettings {
   private static boolean enabled = true;
   private static double minVelocityDelta = 15.0;
   private static double maxVelocityDelta = 120.0;
   private static double maxLaunchSpeed = 128.0;
   private static int cooldownTicks = 60;
   private static boolean affectCreative = true;
   private static boolean debugLogging = true;

   private ReactionSettings() {
   }

   public static boolean enabled() {
      return enabled;
   }

   public static void setEnabled(boolean value) {
      enabled = value;
   }

   public static double minVelocityDelta() {
      return minVelocityDelta;
   }

   public static void setMinVelocityDelta(double value) {
      minVelocityDelta = Math.max(0.1, value);
   }

   public static double maxVelocityDelta() {
      return maxVelocityDelta;
   }

   public static void setMaxVelocityDelta(double value) {
      maxVelocityDelta = Math.max(1.0, value);
   }

   public static double maxLaunchSpeed() {
      return maxLaunchSpeed;
   }

   public static void setMaxLaunchSpeed(double value) {
      maxLaunchSpeed = Math.max(0.5, value);
   }

   public static int cooldownTicks() {
      return cooldownTicks;
   }

   public static void setCooldownTicks(int value) {
      cooldownTicks = Math.max(0, value);
   }

   public static boolean affectCreative() {
      return affectCreative;
   }

   public static void setAffectCreative(boolean value) {
      affectCreative = value;
   }

   public static boolean debugLogging() {
      return debugLogging;
   }

   public static void setDebugLogging(boolean value) {
      debugLogging = value;
   }
}
