package dev.leo.ragdollreactions.neoforge.config;

import dev.leo.ragdollreactions.config.ReactionSettings;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.fml.event.config.ModConfigEvent.Loading;
import net.neoforged.fml.event.config.ModConfigEvent.Reloading;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import net.neoforged.neoforge.common.ModConfigSpec.DoubleValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

public final class ReactionConfig {
   private static final Builder BUILDER = new Builder();

   public static final BooleanValue ENABLED = BUILDER.translation("ragdoll_reactions.configuration.enabled")
      .comment("Master switch for all ragdoll reactions.")
      .define("enabled", true);

   static {
      BUILDER.translation("ragdoll_reactions.configuration.general").comment("Options shared by all triggers.").push("general");
      BUILDER.translation("ragdoll_reactions.configuration.launch").comment("Shared launch velocity and cooldown options.").push("launch");
   }

   public static final DoubleValue MAX_LAUNCH_SPEED = BUILDER.translation("ragdoll_reactions.configuration.max_launch_speed")
      .comment("Clamp total ragdoll launch speed after trigger detection (m/s).")
      .defineInRange("maxSpeed", 128.0, 1.0, 256.0);
   public static final IntValue COOLDOWN_TICKS = BUILDER.translation("ragdoll_reactions.configuration.cooldown_ticks")
      .comment("Ticks before the same player can be auto-ragdolled again.")
      .defineInRange("cooldownTicks", 60, 0, 1200);

   static {
      BUILDER.pop();
      BUILDER.translation("ragdoll_reactions.configuration.targeting").comment("Shared target eligibility options.").push("targeting");
   }

   public static final BooleanValue AFFECT_CREATIVE = BUILDER.translation("ragdoll_reactions.configuration.affect_creative")
      .comment("When true, creative-mode players can also be auto-ragdolled.")
      .define("affectCreative", true);

   static {
      BUILDER.pop();
      BUILDER.translation("ragdoll_reactions.configuration.sound").comment("Shared trigger sound effects.").push("sound");
   }

   public static final BooleanValue TRIGGER_SOUND_ENABLED = BUILDER.translation("ragdoll_reactions.configuration.trigger_sound_enabled")
      .comment("When true, a punch sound plays at the player when any reaction trigger ragdolls them.")
      .define("enabled", true);
   public static final DoubleValue TRIGGER_SOUND_VOLUME = BUILDER.translation("ragdoll_reactions.configuration.trigger_sound_volume")
      .comment("Volume of the trigger sound.")
      .defineInRange("volume", 1.0, 0.0, 4.0);

   static {
      BUILDER.pop();
      BUILDER.translation("ragdoll_reactions.configuration.debug").comment("Developer options.").push("debug");
   }

   public static final BooleanValue DEBUG_LOGGING = BUILDER.translation("ragdoll_reactions.configuration.debug_logging")
      .comment("Log trigger details to the server console.")
      .define("logging", true);

   static {
      BUILDER.pop();
      BUILDER.pop();
      BUILDER.translation("ragdoll_reactions.configuration.suppressions").comment("Player states that suppress automatic reactions.").push("suppressions");
      BUILDER.translation("ragdoll_reactions.configuration.suppression_riptide").comment("Riptide trident self-launch suppression.").push("riptide");
   }

   public static final BooleanValue SUPPRESS_RIPTIDE_ENABLED = BUILDER.translation("ragdoll_reactions.configuration.suppression_enabled")
      .comment("When true, riptide self-launches will not trigger movement reactions.")
      .define("enabled", true);
   public static final IntValue RIPTIDE_GRACE_TICKS = BUILDER.translation("ragdoll_reactions.configuration.grace_ticks")
      .comment("Ticks to keep suppressing after the riptide state ends.")
      .defineInRange("graceTicks", 15, 0, 200);

   static {
      BUILDER.pop();
      BUILDER.translation("ragdoll_reactions.configuration.suppression_bounce").comment("Slime block and bed bounce suppression.").push("bounce");
   }

   public static final BooleanValue SUPPRESS_BOUNCE_ENABLED = BUILDER.translation("ragdoll_reactions.configuration.suppression_enabled")
      .comment("When true, slime block and bed bounces will not trigger movement reactions.")
      .define("enabled", true);
   public static final IntValue BOUNCE_GRACE_TICKS = BUILDER.translation("ragdoll_reactions.configuration.grace_ticks")
      .comment("Ticks to keep suppressing after the player leaves a bounce block.")
      .defineInRange("graceTicks", 10, 0, 200);

   static {
      BUILDER.pop();
      BUILDER.translation("ragdoll_reactions.configuration.suppression_elytra_flight").comment("Elytra flight movement suppression.").push("elytraFlight");
   }

   public static final BooleanValue SUPPRESS_ELYTRA_FLIGHT_ENABLED = BUILDER.translation("ragdoll_reactions.configuration.suppression_enabled")
      .comment("When true, elytra flight and its landing deceleration will not trigger movement reactions.")
      .define("enabled", true);
   public static final IntValue ELYTRA_FLIGHT_GRACE_TICKS = BUILDER.translation("ragdoll_reactions.configuration.grace_ticks")
      .comment("Ticks to keep suppressing after the player stops fall-flying.")
      .defineInRange("graceTicks", 10, 0, 200);

   static {
      BUILDER.pop();
      BUILDER.translation("ragdoll_reactions.configuration.suppression_creative_flight").comment("Creative/spectator-style flight movement suppression.").push("creativeFlight");
   }

   public static final BooleanValue SUPPRESS_CREATIVE_FLIGHT_ENABLED = BUILDER.translation("ragdoll_reactions.configuration.suppression_enabled")
      .comment("When true, creative flight and its landing deceleration will not trigger movement reactions.")
      .define("enabled", true);
   public static final IntValue CREATIVE_FLIGHT_GRACE_TICKS = BUILDER.translation("ragdoll_reactions.configuration.grace_ticks")
      .comment("Ticks to keep suppressing after the player stops flying.")
      .defineInRange("graceTicks", 10, 0, 200);

   static {
      BUILDER.pop();
      BUILDER.translation("ragdoll_reactions.configuration.suppression_rope_climbing").comment("Create Aeronautics Climbable Ropes suppression.").push("ropeClimbing");
   }

   public static final BooleanValue SUPPRESS_ROPE_CLIMBING_ENABLED = BUILDER.translation("ragdoll_reactions.configuration.suppression_enabled")
      .comment("When true, climbing ropes from Create Aeronautics Climbable Ropes will not trigger movement reactions. Requires Create/Simulated.")
      .define("enabled", true);
   public static final IntValue ROPE_CLIMBING_GRACE_TICKS = BUILDER.translation("ragdoll_reactions.configuration.grace_ticks")
      .comment("Ticks to keep suppressing after the player stops climbing a rope.")
      .defineInRange("graceTicks", 10, 0, 200);

   static {
      BUILDER.pop();
      BUILDER.translation("ragdoll_reactions.configuration.suppression_chain_conveyor").comment("Create chain conveyor riding suppression.").push("chainConveyor");
   }

   public static final BooleanValue SUPPRESS_CHAIN_CONVEYOR_ENABLED = BUILDER.translation("ragdoll_reactions.configuration.suppression_enabled")
      .comment("When true, Create chain conveyor riding will not trigger movement reactions. Requires Create.")
      .define("enabled", true);
   public static final IntValue CHAIN_CONVEYOR_GRACE_TICKS = BUILDER.translation("ragdoll_reactions.configuration.grace_ticks")
      .comment("Ticks to keep suppressing after the player stops hanging from a chain conveyor.")
      .defineInRange("graceTicks", 10, 0, 200);

   static {
      BUILDER.pop();
      BUILDER.translation("ragdoll_reactions.configuration.suppression_wind_charge").comment("Wind charge reaction suppression.").push("windCharge");
   }

   public static final BooleanValue SUPPRESS_ALL_WIND_CHARGES = BUILDER.translation("ragdoll_reactions.configuration.suppression_all_wind_charges")
      .comment("When true, no wind charge explosions (from any source) will ragdoll players.")
      .define("suppressAll", false);
   public static final BooleanValue SUPPRESS_SELF_WIND_CHARGE = BUILDER.translation("ragdoll_reactions.configuration.suppression_self_wind_charge")
      .comment("When true, self-thrown wind charges will not ragdoll the throwing player.")
      .define("suppressSelf", true);
   public static final IntValue WIND_CHARGE_GRACE_TICKS = BUILDER.translation("ragdoll_reactions.configuration.grace_ticks")
      .comment("Ticks to suppress movement reactions after a self-thrown wind charge explodes.")
      .defineInRange("graceTicks", 10, 0, 200);

   static {
      BUILDER.pop();
      BUILDER.pop();
      BUILDER.translation("ragdoll_reactions.configuration.triggers").comment("Reaction trigger sources.").push("triggers");
      BUILDER.translation("ragdoll_reactions.configuration.trigger_impact").comment("Sharp horizontal velocity change reactions.").push("impact");
   }

   public static final BooleanValue IMPACT_ENABLED = BUILDER.translation("ragdoll_reactions.configuration.trigger_enabled")
      .comment("When true, sharp horizontal velocity changes can ragdoll the player.")
      .define("enabled", true);
   public static final DoubleValue MIN_VELOCITY_DELTA = BUILDER.translation("ragdoll_reactions.configuration.min_velocity_delta")
      .comment("Minimum horizontal player velocity change over the 5-tick window (m/s) required to trigger.")
      .defineInRange("minVelocityDelta", 20.0, 0.1, 128.0);
   public static final DoubleValue MAX_VELOCITY_DELTA = BUILDER.translation("ragdoll_reactions.configuration.max_velocity_delta")
      .comment("Velocity changes above this (m/s) are ignored as teleports or anomalies rather than physical impacts.")
      .defineInRange("maxVelocityDelta", 120.0, 1.0, 1024.0);

   static {
      BUILDER.pop();
      BUILDER.translation("ragdoll_reactions.configuration.trigger_cannon_explosions").comment("Create Big Cannons explosion reactions.").push("cannonExplosions");
   }

   public static final BooleanValue CANNON_EXPLOSIONS_ENABLED = BUILDER.translation("ragdoll_reactions.configuration.trigger_enabled")
      .comment("When true, Create Big Cannons custom explosions can ragdoll nearby players.")
      .define("enabled", true);
   public static final DoubleValue MIN_CANNON_EXPLOSION_POWER = BUILDER.translation("ragdoll_reactions.configuration.min_cannon_explosion_power")
      .comment("Minimum Create Big Cannons explosion power required to trigger a ragdoll.")
      .defineInRange("minPower", 1.0, 0.0, 256.0);
   public static final DoubleValue CANNON_EXPLOSION_RADIUS_PADDING = BUILDER.translation("ragdoll_reactions.configuration.cannon_explosion_radius_padding")
      .comment("Extra blocks added to the explosion entity radius when searching for nearby players.")
      .defineInRange("radiusPadding", 2.0, 0.0, 64.0);
   public static final DoubleValue CANNON_EXPLOSION_LAUNCH_MULTIPLIER = BUILDER.translation("ragdoll_reactions.configuration.cannon_explosion_launch_multiplier")
      .comment("Launch speed multiplier applied to cannon explosion power before the global max launch speed clamp.")
      .defineInRange("launchMultiplier", 20.0, 0.0, 128.0);

   static {
      BUILDER.pop();
      BUILDER.translation("ragdoll_reactions.configuration.trigger_vanilla_explosions").comment("Vanilla explosion reactions.").push("vanillaExplosions");
   }

   public static final BooleanValue VANILLA_EXPLOSIONS_ENABLED = BUILDER.translation("ragdoll_reactions.configuration.trigger_enabled")
      .comment("When true, vanilla explosions can ragdoll nearby players.")
      .define("enabled", true);
   public static final DoubleValue MIN_VANILLA_EXPLOSION_POWER = BUILDER.translation("ragdoll_reactions.configuration.min_vanilla_explosion_power")
      .comment("Minimum explosion power required to trigger a ragdoll.")
      .defineInRange("minPower", 1.0, 0.0, 256.0);
   public static final DoubleValue VANILLA_EXPLOSION_RADIUS_PADDING = BUILDER.translation("ragdoll_reactions.configuration.vanilla_explosion_radius_padding")
      .comment("Extra blocks added to the explosion knockback radius when searching for nearby players.")
      .defineInRange("radiusPadding", 2.0, 0.0, 64.0);
   public static final DoubleValue VANILLA_EXPLOSION_LAUNCH_MULTIPLIER = BUILDER.translation("ragdoll_reactions.configuration.vanilla_explosion_launch_multiplier")
      .comment("Launch speed multiplier applied to explosion power before the global max launch speed clamp.")
      .defineInRange("launchMultiplier", 6.0, 0.0, 128.0);

   static {
      BUILDER.pop();
      BUILDER.translation("ragdoll_reactions.configuration.trigger_fall").comment("Hard fall landing reactions.").push("fall");
   }

   public static final BooleanValue FALL_REACTIONS_ENABLED = BUILDER.translation("ragdoll_reactions.configuration.trigger_enabled")
      .comment("When true, landing from a high fall ragdolls the player.")
      .define("enabled", true);
   public static final DoubleValue MIN_FALL_DAMAGE = BUILDER.translation("ragdoll_reactions.configuration.min_fall_damage")
      .comment("Minimum fall damage actually taken (after feather falling, protection, resistance, etc.) required to trigger a ragdoll.")
      .defineInRange("minDamage", 4.0, 0.0, 1024.0);
   public static final DoubleValue FALL_SLAM_MULTIPLIER = BUILDER.translation("ragdoll_reactions.configuration.fall_slam_multiplier")
      .comment("Fraction of the landing impact speed driven downward to smash the ragdoll into the ground.")
      .defineInRange("slamMultiplier", 0.5, 0.0, 2.0);

   static {
      BUILDER.pop();
      BUILDER.translation("ragdoll_reactions.configuration.trigger_crash").comment("Elytra wall-crash reactions.").push("crash");
   }

   public static final BooleanValue CRASH_IMPACTS_ENABLED = BUILDER.translation("ragdoll_reactions.configuration.trigger_enabled")
      .comment("When true, flying into a wall while gliding ragdolls the player.")
      .define("enabled", true);
   public static final DoubleValue MIN_CRASH_DAMAGE = BUILDER.translation("ragdoll_reactions.configuration.min_crash_damage")
      .comment("Minimum fly-into-wall damage required to trigger a ragdoll.")
      .defineInRange("minDamage", 4.0, 0.0, 1024.0);
   public static final DoubleValue CRASH_LAUNCH_MULTIPLIER = BUILDER.translation("ragdoll_reactions.configuration.crash_launch_multiplier")
      .comment("Forward crash drive speed (m/s) per point of crash damage, scaled down before launch.")
      .defineInRange("launchMultiplier", 3.0, 0.0, 64.0);

   static {
      BUILDER.pop();
      BUILDER.translation("ragdoll_reactions.configuration.trigger_hit").comment("Heavy melee/projectile hit reactions.").push("hit");
   }

   public static final BooleanValue HIT_REACTIONS_ENABLED = BUILDER.translation("ragdoll_reactions.configuration.trigger_enabled")
      .comment("When true, taking a big hit knocks the player into a ragdoll away from the attacker.")
      .define("enabled", true);
   public static final DoubleValue MIN_HIT_DAMAGE = BUILDER.translation("ragdoll_reactions.configuration.min_hit_damage")
      .comment("Minimum damage required to trigger a ragdoll.")
      .defineInRange("minDamage", 8.0, 0.0, 1024.0);
   public static final DoubleValue HIT_LAUNCH_MULTIPLIER = BUILDER.translation("ragdoll_reactions.configuration.hit_launch_multiplier")
      .comment("Launch speed (m/s) per point of damage taken.")
      .defineInRange("launchMultiplier", 1.5, 0.0, 64.0);

   static {
      BUILDER.pop();
      BUILDER.translation("ragdoll_reactions.configuration.trigger_lightning").comment("Lightning strike reactions.").push("lightning");
   }

   public static final BooleanValue LIGHTNING_REACTIONS_ENABLED = BUILDER.translation("ragdoll_reactions.configuration.trigger_enabled")
      .comment("When true, being struck by lightning ragdolls the player.")
      .define("enabled", true);
   public static final DoubleValue LIGHTNING_LAUNCH_SPEED = BUILDER.translation("ragdoll_reactions.configuration.lightning_launch_speed")
      .comment("Upward launch speed (m/s) when struck by lightning.")
      .defineInRange("launchSpeed", 12.0, 0.0, 128.0);

   static {
      BUILDER.pop();
      BUILDER.pop();
   }

   public static final ModConfigSpec SPEC = BUILDER.build();

   private ReactionConfig() {
   }

   public static void register(ModContainer container) {
      container.registerConfig(Type.SERVER, SPEC);
   }

   public static void onLoad(Loading event) {
      if (event.getConfig().getSpec() == SPEC) {
         apply();
      }
   }

   public static void onReload(Reloading event) {
      if (event.getConfig().getSpec() == SPEC) {
         apply();
      }
   }

   private static void apply() {
      ReactionSettings.setEnabled((Boolean) ENABLED.get());

      ReactionSettings.General general = ReactionSettings.general();
      general.launch().setMaxSpeed((Double) MAX_LAUNCH_SPEED.get());
      general.launch().setCooldownTicks((Integer) COOLDOWN_TICKS.get());
      general.targeting().setAffectCreative((Boolean) AFFECT_CREATIVE.get());
      general.sound().setEnabled((Boolean) TRIGGER_SOUND_ENABLED.get());
      general.sound().setVolume((Double) TRIGGER_SOUND_VOLUME.get());
      general.debug().setLogging((Boolean) DEBUG_LOGGING.get());

      ReactionSettings.Suppressions suppressions = ReactionSettings.suppressions();
      suppressions.riptide().setEnabled((Boolean) SUPPRESS_RIPTIDE_ENABLED.get());
      suppressions.riptide().setGraceTicks((Integer) RIPTIDE_GRACE_TICKS.get());
      suppressions.bounce().setEnabled((Boolean) SUPPRESS_BOUNCE_ENABLED.get());
      suppressions.bounce().setGraceTicks((Integer) BOUNCE_GRACE_TICKS.get());
      suppressions.elytraFlight().setEnabled((Boolean) SUPPRESS_ELYTRA_FLIGHT_ENABLED.get());
      suppressions.elytraFlight().setGraceTicks((Integer) ELYTRA_FLIGHT_GRACE_TICKS.get());
      suppressions.creativeFlight().setEnabled((Boolean) SUPPRESS_CREATIVE_FLIGHT_ENABLED.get());
      suppressions.creativeFlight().setGraceTicks((Integer) CREATIVE_FLIGHT_GRACE_TICKS.get());
      suppressions.ropeClimbing().setEnabled((Boolean) SUPPRESS_ROPE_CLIMBING_ENABLED.get());
      suppressions.ropeClimbing().setGraceTicks((Integer) ROPE_CLIMBING_GRACE_TICKS.get());
      suppressions.chainConveyor().setEnabled((Boolean) SUPPRESS_CHAIN_CONVEYOR_ENABLED.get());
      suppressions.chainConveyor().setGraceTicks((Integer) CHAIN_CONVEYOR_GRACE_TICKS.get());
      suppressions.windCharge().setSuppressAll((Boolean) SUPPRESS_ALL_WIND_CHARGES.get());
      suppressions.windCharge().setSuppressSelf((Boolean) SUPPRESS_SELF_WIND_CHARGE.get());
      suppressions.windCharge().setGraceTicks((Integer) WIND_CHARGE_GRACE_TICKS.get());

      ReactionSettings.Triggers triggers = ReactionSettings.triggers();
      triggers.impact().setEnabled((Boolean) IMPACT_ENABLED.get());
      triggers.impact().setMinVelocityDelta((Double) MIN_VELOCITY_DELTA.get());
      triggers.impact().setMaxVelocityDelta((Double) MAX_VELOCITY_DELTA.get());
      triggers.cannonExplosions().setEnabled((Boolean) CANNON_EXPLOSIONS_ENABLED.get());
      triggers.cannonExplosions().setMinPower((Double) MIN_CANNON_EXPLOSION_POWER.get());
      triggers.cannonExplosions().setRadiusPadding((Double) CANNON_EXPLOSION_RADIUS_PADDING.get());
      triggers.cannonExplosions().setLaunchMultiplier((Double) CANNON_EXPLOSION_LAUNCH_MULTIPLIER.get());
      triggers.vanillaExplosions().setEnabled((Boolean) VANILLA_EXPLOSIONS_ENABLED.get());
      triggers.vanillaExplosions().setMinPower((Double) MIN_VANILLA_EXPLOSION_POWER.get());
      triggers.vanillaExplosions().setRadiusPadding((Double) VANILLA_EXPLOSION_RADIUS_PADDING.get());
      triggers.vanillaExplosions().setLaunchMultiplier((Double) VANILLA_EXPLOSION_LAUNCH_MULTIPLIER.get());
      triggers.fall().setEnabled((Boolean) FALL_REACTIONS_ENABLED.get());
      triggers.fall().setMinDamage((Double) MIN_FALL_DAMAGE.get());
      triggers.fall().setSlamMultiplier((Double) FALL_SLAM_MULTIPLIER.get());
      triggers.crash().setEnabled((Boolean) CRASH_IMPACTS_ENABLED.get());
      triggers.crash().setMinDamage((Double) MIN_CRASH_DAMAGE.get());
      triggers.crash().setLaunchMultiplier((Double) CRASH_LAUNCH_MULTIPLIER.get());
      triggers.hit().setEnabled((Boolean) HIT_REACTIONS_ENABLED.get());
      triggers.hit().setMinDamage((Double) MIN_HIT_DAMAGE.get());
      triggers.hit().setLaunchMultiplier((Double) HIT_LAUNCH_MULTIPLIER.get());
      triggers.lightning().setEnabled((Boolean) LIGHTNING_REACTIONS_ENABLED.get());
      triggers.lightning().setLaunchSpeed((Double) LIGHTNING_LAUNCH_SPEED.get());
   }
}
