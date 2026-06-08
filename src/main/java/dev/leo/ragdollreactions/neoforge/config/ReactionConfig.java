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
      .comment("Master switch for the accel/decel ragdoll trigger.")
      .define("enabled", true);

   static {
      BUILDER.translation("ragdoll_reactions.configuration.trigger").comment("When a sublevel impact ragdolls the player.").push("trigger");
   }

   public static final DoubleValue MIN_SUBLEVEL_SPEED = BUILDER.translation("ragdoll_reactions.configuration.min_sublevel_speed")
      .comment(
         "Minimum speed (m/s) of a sub-level at the player's position to consider an impact. Matches sable's own damage threshold (3.0)."
      )
      .defineInRange("minSubLevelSpeed", 3.0, 0.1, 64.0);
   public static final DoubleValue MIN_VELOCITY_DELTA = BUILDER.translation("ragdoll_reactions.configuration.min_velocity_delta")
      .comment(
         "Minimum horizontal player velocity change over the 5-tick window (m/s) required to confirm the impact was felt."
      )
      .defineInRange("minVelocityDelta", 5.0, 0.1, 64.0);
   public static final IntValue COOLDOWN_TICKS = BUILDER.translation("ragdoll_reactions.configuration.cooldown_ticks")
      .comment("Ticks before the same player can be auto-ragdolled again.")
      .defineInRange("cooldownTicks", 60, 0, 1200);
   public static final BooleanValue AFFECT_CREATIVE = BUILDER.translation("ragdoll_reactions.configuration.affect_creative")
      .comment("When true, creative-mode players can also be auto-ragdolled.")
      .define("affectCreative", true);

   static {
      BUILDER.pop();
      BUILDER.translation("ragdoll_reactions.configuration.launch").comment("Launch velocity limits.").push("launch");
   }

   public static final DoubleValue MAX_LAUNCH_SPEED = BUILDER.translation("ragdoll_reactions.configuration.max_launch_speed")
      .comment(
         "Clamp total ragdoll launch speed after trigger detection (m/s). Above ~128 chunks cannot load fast enough to keep up."
      )
      .defineInRange("maxLaunchSpeed", 128.0, 1.0, 256.0);

   static {
      BUILDER.pop();
      BUILDER.translation("ragdoll_reactions.configuration.debug").comment("Developer options.").push("debug");
   }

   public static final BooleanValue DEBUG_LOGGING = BUILDER.translation("ragdoll_reactions.configuration.debug_logging")
      .comment("Log trigger details to the server console.")
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
      ReactionSettings.setMinSubLevelSpeed((Double) MIN_SUBLEVEL_SPEED.get());
      ReactionSettings.setMinVelocityDelta((Double) MIN_VELOCITY_DELTA.get());
      ReactionSettings.setCooldownTicks((Integer) COOLDOWN_TICKS.get());
      ReactionSettings.setAffectCreative((Boolean) AFFECT_CREATIVE.get());
      ReactionSettings.setMaxLaunchSpeed((Double) MAX_LAUNCH_SPEED.get());
      ReactionSettings.setDebugLogging((Boolean) DEBUG_LOGGING.get());
   }
}
