package dev.leo.ragdollreactions.physics;

import dev.leo.ragdollreactions.config.ReactionSettings;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class ReactionSuppressions {
   private static final Map<UUID, Long> SUPPRESS_UNTIL_TICK = new HashMap<>();

   private static boolean triedChainConveyorReflection;
   private static Field chainConveyorHangingPlayersField;

   private ReactionSuppressions() {
   }

   public static boolean isTemporarilySuppressed(ServerLevel level, ServerPlayer player, long gameTime) {
      UUID playerId = player.getUUID();

      int grace = 0;
      ReactionSettings.Suppressions suppressions = ReactionSettings.suppressions();
      if (suppressions.riptide().enabled() && player.isAutoSpinAttack()) {
         grace = suppressions.riptide().graceTicks();
      } else if (suppressions.bounce().enabled() && isOnBounceBlock(level, player)) {
         grace = suppressions.bounce().graceTicks();
      } else if (suppressions.elytraFlight().enabled() && player.isFallFlying()) {
         grace = suppressions.elytraFlight().graceTicks();
      } else if (suppressions.creativeFlight().enabled() && player.getAbilities().flying) {
         grace = suppressions.creativeFlight().graceTicks();
      } else if (suppressions.ropeClimbing().enabled() && isMarkedAsRopeRiding(player)) {
         grace = suppressions.ropeClimbing().graceTicks();
      } else if (suppressions.chainConveyor().enabled() && isHangingOnChainConveyor(player)) {
         grace = suppressions.chainConveyor().graceTicks();
      }

      if (grace > 0) {
         SUPPRESS_UNTIL_TICK.put(playerId, gameTime + grace);
         return true;
      }

      Long until = SUPPRESS_UNTIL_TICK.get(playerId);
      if (until == null) {
         return false;
      }
      if (gameTime < until) {
         return true;
      }
      SUPPRESS_UNTIL_TICK.remove(playerId);
      return false;
   }

   public static void clear(UUID playerId) {
      SUPPRESS_UNTIL_TICK.remove(playerId);
   }

   public static void suppress(ServerPlayer player, long gameTime, int graceTicks) {
      if (graceTicks > 0) {
         SUPPRESS_UNTIL_TICK.put(player.getUUID(), gameTime + graceTicks);
      }
   }

   public static void reset() {
      SUPPRESS_UNTIL_TICK.clear();
   }

   static boolean isOnBounceBlock(ServerLevel level, ServerPlayer player) {
      BlockState below = level.getBlockState(player.blockPosition().below());
      return below.is(Blocks.SLIME_BLOCK) || below.getBlock() instanceof BedBlock;
   }

   private static boolean isHangingOnChainConveyor(ServerPlayer player) {
      return isMarkedAsRopeRiding(player);
   }

   private static boolean isMarkedAsRopeRiding(ServerPlayer player) {
      if (!triedChainConveyorReflection) {
         triedChainConveyorReflection = true;
         try {
            Class<?> handlerClass = Class.forName("com.simibubi.create.content.kinetics.chainConveyor.ServerChainConveyorHandler");
            chainConveyorHangingPlayersField = handlerClass.getDeclaredField("hangingPlayers");
            chainConveyorHangingPlayersField.setAccessible(true);
         } catch (LinkageError | ReflectiveOperationException | SecurityException ignored) {
            chainConveyorHangingPlayersField = null;
         }
      }

      if (chainConveyorHangingPlayersField == null) {
         return false;
      }

      try {
         Object value = chainConveyorHangingPlayersField.get(null);
         if (value instanceof Map<?, ?> hangingPlayers) {
            return hangingPlayers.containsKey(player.getUUID());
         }
      } catch (LinkageError | ReflectiveOperationException | SecurityException ignored) {
         chainConveyorHangingPlayersField = null;
      }

      return false;
   }
}
