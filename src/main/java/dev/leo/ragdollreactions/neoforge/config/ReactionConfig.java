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

   public static final BooleanValue ENABLED = BUILDER.comment("Master switch for the accel/decel ragdoll trigger.")
      .define("enabled", true);

   static {
      BUILDER.comment("When a sharp velocity change ragdolls the player.").push("trigger");
   }

   public static final DoubleValue MIN_VELOCITY_DELTA = BUILDER.comment(
         "Minimum player velocity change over the 5-tick window (m/s). Covers braking, acceleration, and direction changes."
      )
      .defineInRange("minVelocityDelta", 15.0, 0.5, 64.0);
   public static final DoubleValue MAX_VELOCITY_DELTA = BUILDER.comment("Ignore velocity spikes above this (m/s) to filter teleports and chunk loads.")
      .defineInRange("maxVelocityDelta", 120.0, 8.0, 256.0);
   public static final IntValue COOLDOWN_TICKS = BUILDER.comment("Ticks before the same player can be auto-ragdolled again.")
      .defineInRange("cooldownTicks", 60, 0, 1200);
   public static final BooleanValue AFFECT_CREATIVE = BUILDER.comment("When true, creative-mode players can also be auto-ragdolled.")
      .define("affectCreative", true);

   static {
      BUILDER.pop();
      BUILDER.comment("Launch velocity limits.").push("launch");
   }

   public static final DoubleValue MAX_LAUNCH_SPEED = BUILDER.comment(
         "Clamp total ragdoll launch speed after trigger detection (m/s). Above ~128 chunks cannot load fast enough to keep up."
      )
      .defineInRange("maxLaunchSpeed", 128.0, 1.0, 256.0);

   static {
      BUILDER.pop();
      BUILDER.comment("Developer options.").push("debug");
   }

   public static final BooleanValue DEBUG_LOGGING = BUILDER.comment("Log trigger details to the server console.")
      .define("debugLogging", true);

   static {
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
      ReactionSettings.setMinVelocityDelta((Double) MIN_VELOCITY_DELTA.get());
      ReactionSettings.setMaxVelocityDelta((Double) MAX_VELOCITY_DELTA.get());
      ReactionSettings.setCooldownTicks((Integer) COOLDOWN_TICKS.get());
      ReactionSettings.setAffectCreative((Boolean) AFFECT_CREATIVE.get());
      ReactionSettings.setMaxLaunchSpeed((Double) MAX_LAUNCH_SPEED.get());
      ReactionSettings.setDebugLogging((Boolean) DEBUG_LOGGING.get());
   }
}
