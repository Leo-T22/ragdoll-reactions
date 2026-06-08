package dev.leo.ragdollreactions.config;

public final class ReactionSettings {
   private static boolean enabled = true;
   private static double minSubLevelSpeed = 3.0;
   private static double minVelocityDelta = 5.0;
   private static boolean cannonExplosionsEnabled = true;
   private static double minCannonExplosionPower = 1.0;
   private static double cannonExplosionRadiusPadding = 2.0;
   private static double cannonExplosionLaunchMultiplier = 20.0;
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

   public static double minSubLevelSpeed() {
      return minSubLevelSpeed;
   }

   public static void setMinSubLevelSpeed(double value) {
      minSubLevelSpeed = Math.max(0.1, value);
   }

   public static double minVelocityDelta() {
      return minVelocityDelta;
   }

   public static void setMinVelocityDelta(double value) {
      minVelocityDelta = Math.max(0.1, value);
   }

   public static boolean cannonExplosionsEnabled() {
      return cannonExplosionsEnabled;
   }

   public static void setCannonExplosionsEnabled(boolean value) {
      cannonExplosionsEnabled = value;
   }

   public static double minCannonExplosionPower() {
      return minCannonExplosionPower;
   }

   public static void setMinCannonExplosionPower(double value) {
      minCannonExplosionPower = Math.max(0.0, value);
   }

   public static double cannonExplosionRadiusPadding() {
      return cannonExplosionRadiusPadding;
   }

   public static void setCannonExplosionRadiusPadding(double value) {
      cannonExplosionRadiusPadding = Math.max(0.0, value);
   }

   public static double cannonExplosionLaunchMultiplier() {
      return cannonExplosionLaunchMultiplier;
   }

   public static void setCannonExplosionLaunchMultiplier(double value) {
      cannonExplosionLaunchMultiplier = Math.max(0.0, value);
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
