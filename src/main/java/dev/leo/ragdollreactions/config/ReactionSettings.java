package dev.leo.ragdollreactions.config;

public final class ReactionSettings {
   private static boolean enabled = true;

   private static final General GENERAL = new General();
   private static final Suppressions SUPPRESSIONS = new Suppressions();
   private static final Triggers TRIGGERS = new Triggers();

   private ReactionSettings() {
   }

   public static boolean enabled() {
      return enabled;
   }

   public static void setEnabled(boolean value) {
      enabled = value;
   }

   public static General general() {
      return GENERAL;
   }

   public static Suppressions suppressions() {
      return SUPPRESSIONS;
   }

   public static Triggers triggers() {
      return TRIGGERS;
   }

   public static final class General {
      private final Launch launch = new Launch();
      private final Targeting targeting = new Targeting();
      private final Sound sound = new Sound();
      private final Debug debug = new Debug();

      private General() {
      }

      public Launch launch() {
         return launch;
      }

      public Targeting targeting() {
         return targeting;
      }

      public Sound sound() {
         return sound;
      }

      public Debug debug() {
         return debug;
      }
   }

   public static final class Launch {
      private double maxSpeed = 128.0;
      private int cooldownTicks = 60;

      private Launch() {
      }

      public double maxSpeed() {
         return maxSpeed;
      }

      public void setMaxSpeed(double value) {
         maxSpeed = Math.max(0.5, value);
      }

      public int cooldownTicks() {
         return cooldownTicks;
      }

      public void setCooldownTicks(int value) {
         cooldownTicks = Math.max(0, value);
      }
   }

   public static final class Targeting {
      private boolean affectCreative = true;

      private Targeting() {
      }

      public boolean affectCreative() {
         return affectCreative;
      }

      public void setAffectCreative(boolean value) {
         affectCreative = value;
      }
   }

   public static final class Sound {
      private boolean enabled = true;
      private double volume = 1.0;

      private Sound() {
      }

      public boolean enabled() {
         return enabled;
      }

      public void setEnabled(boolean value) {
         enabled = value;
      }

      public double volume() {
         return volume;
      }

      public void setVolume(double value) {
         volume = Math.max(0.0, value);
      }
   }

   public static final class Debug {
      private boolean logging = true;

      private Debug() {
      }

      public boolean logging() {
         return logging;
      }

      public void setLogging(boolean value) {
         logging = value;
      }
   }

   public static final class Suppressions {
      private final Suppression riptide = new Suppression(true, 15);
      private final Suppression bounce = new Suppression(true, 10);
      private final Suppression elytraFlight = new Suppression(true, 10);
      private final Suppression ropeClimbing = new Suppression(true, 10);
      private final Suppression chainConveyor = new Suppression(true, 10);

      private Suppressions() {
      }

      public Suppression riptide() {
         return riptide;
      }

      public Suppression bounce() {
         return bounce;
      }

      public Suppression elytraFlight() {
         return elytraFlight;
      }

      public Suppression ropeClimbing() {
         return ropeClimbing;
      }

      public Suppression chainConveyor() {
         return chainConveyor;
      }
   }

   public static final class Suppression {
      private boolean enabled;
      private int graceTicks;

      private Suppression(boolean enabled, int graceTicks) {
         this.enabled = enabled;
         this.graceTicks = graceTicks;
      }

      public boolean enabled() {
         return enabled;
      }

      public void setEnabled(boolean value) {
         enabled = value;
      }

      public int graceTicks() {
         return graceTicks;
      }

      public void setGraceTicks(int value) {
         graceTicks = Math.max(0, value);
      }
   }

   public static final class Triggers {
      private final Impact impact = new Impact();
      private final CannonExplosions cannonExplosions = new CannonExplosions();
      private final VanillaExplosions vanillaExplosions = new VanillaExplosions();
      private final Fall fall = new Fall();
      private final Crash crash = new Crash();
      private final Hit hit = new Hit();
      private final Lightning lightning = new Lightning();

      private Triggers() {
      }

      public Impact impact() {
         return impact;
      }

      public CannonExplosions cannonExplosions() {
         return cannonExplosions;
      }

      public VanillaExplosions vanillaExplosions() {
         return vanillaExplosions;
      }

      public Fall fall() {
         return fall;
      }

      public Crash crash() {
         return crash;
      }

      public Hit hit() {
         return hit;
      }

      public Lightning lightning() {
         return lightning;
      }
   }

   public static final class Impact {
      private boolean enabled = true;
      private double minVelocityDelta = 15.0;
      private double maxVelocityDelta = 120.0;

      private Impact() {
      }

      public boolean enabled() {
         return enabled;
      }

      public void setEnabled(boolean value) {
         enabled = value;
      }

      public double minVelocityDelta() {
         return minVelocityDelta;
      }

      public void setMinVelocityDelta(double value) {
         minVelocityDelta = Math.max(0.1, value);
      }

      public double maxVelocityDelta() {
         return maxVelocityDelta;
      }

      public void setMaxVelocityDelta(double value) {
         maxVelocityDelta = Math.max(1.0, value);
      }
   }

   public static final class CannonExplosions {
      private boolean enabled = true;
      private double minPower = 1.0;
      private double radiusPadding = 2.0;
      private double launchMultiplier = 20.0;

      private CannonExplosions() {
      }

      public boolean enabled() {
         return enabled;
      }

      public void setEnabled(boolean value) {
         enabled = value;
      }

      public double minPower() {
         return minPower;
      }

      public void setMinPower(double value) {
         minPower = Math.max(0.0, value);
      }

      public double radiusPadding() {
         return radiusPadding;
      }

      public void setRadiusPadding(double value) {
         radiusPadding = Math.max(0.0, value);
      }

      public double launchMultiplier() {
         return launchMultiplier;
      }

      public void setLaunchMultiplier(double value) {
         launchMultiplier = Math.max(0.0, value);
      }
   }

   public static final class VanillaExplosions {
      private boolean enabled = true;
      private double minPower = 1.0;
      private double radiusPadding = 2.0;
      private double launchMultiplier = 6.0;

      private VanillaExplosions() {
      }

      public boolean enabled() {
         return enabled;
      }

      public void setEnabled(boolean value) {
         enabled = value;
      }

      public double minPower() {
         return minPower;
      }

      public void setMinPower(double value) {
         minPower = Math.max(0.0, value);
      }

      public double radiusPadding() {
         return radiusPadding;
      }

      public void setRadiusPadding(double value) {
         radiusPadding = Math.max(0.0, value);
      }

      public double launchMultiplier() {
         return launchMultiplier;
      }

      public void setLaunchMultiplier(double value) {
         launchMultiplier = Math.max(0.0, value);
      }
   }

   public static final class Fall {
      private boolean enabled = true;
      private double minDistance = 6.0;
      private double slamMultiplier = 0.5;

      private Fall() {
      }

      public boolean enabled() {
         return enabled;
      }

      public void setEnabled(boolean value) {
         enabled = value;
      }

      public double minDistance() {
         return minDistance;
      }

      public void setMinDistance(double value) {
         minDistance = Math.max(1.0, value);
      }

      public double slamMultiplier() {
         return slamMultiplier;
      }

      public void setSlamMultiplier(double value) {
         slamMultiplier = Math.max(0.0, value);
      }
   }

   public static final class Crash {
      private boolean enabled = true;
      private double minDamage = 4.0;
      private double launchMultiplier = 3.0;

      private Crash() {
      }

      public boolean enabled() {
         return enabled;
      }

      public void setEnabled(boolean value) {
         enabled = value;
      }

      public double minDamage() {
         return minDamage;
      }

      public void setMinDamage(double value) {
         minDamage = Math.max(0.0, value);
      }

      public double launchMultiplier() {
         return launchMultiplier;
      }

      public void setLaunchMultiplier(double value) {
         launchMultiplier = Math.max(0.0, value);
      }
   }

   public static final class Hit {
      private boolean enabled = true;
      private double minDamage = 8.0;
      private double launchMultiplier = 1.5;

      private Hit() {
      }

      public boolean enabled() {
         return enabled;
      }

      public void setEnabled(boolean value) {
         enabled = value;
      }

      public double minDamage() {
         return minDamage;
      }

      public void setMinDamage(double value) {
         minDamage = Math.max(0.0, value);
      }

      public double launchMultiplier() {
         return launchMultiplier;
      }

      public void setLaunchMultiplier(double value) {
         launchMultiplier = Math.max(0.0, value);
      }
   }

   public static final class Lightning {
      private boolean enabled = true;
      private double launchSpeed = 12.0;

      private Lightning() {
      }

      public boolean enabled() {
         return enabled;
      }

      public void setEnabled(boolean value) {
         enabled = value;
      }

      public double launchSpeed() {
         return launchSpeed;
      }

      public void setLaunchSpeed(double value) {
         launchSpeed = Math.max(0.0, value);
      }
   }
}
