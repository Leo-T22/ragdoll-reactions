package dev.leo.ragdollreactions.physics;

import dev.leo.ragdollreactions.RagdollReactions;
import dev.leo.ragdollreactions.config.ReactionSettings;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.mixinterface.entity.entity_sublevel_collision.EntityMovementExtension;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.entity_collision.SubLevelEntityCollision;
import java.lang.reflect.Method;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.joml.Vector3d;

public final class MobImpactReactionHandler {
   private static final double BLOCKS_PER_TICK_TO_METERS_PER_SECOND = 20.0;

   private MobImpactReactionHandler() {
   }

   public static void onMobTick(Mob mob) {
      if (!ReactionSettings.enabled() || !ReactionSettings.mobs().enabled()) {
         return;
      }
      ReactionSettings.MobImpact impact = ReactionSettings.mobs().impact();
      if (!impact.enabled()) {
         return;
      }
      if (!(mob.level() instanceof ServerLevel level)) {
         return;
      }

      SubLevelEntityCollision.CollisionInfo info = ((EntityMovementExtension) mob).sable$getCollisionInfo();
      if (info == null || info.firstCollisions == null || info.firstCollisions.isEmpty()) {
         return;
      }

      long gameTime = level.getGameTime();
      if (!ReactionMobLauncher.canTarget(mob, gameTime)) {
         return;
      }

      for (var entry : info.firstCollisions.entrySet()) {
         SubLevel subLevel = entry.getKey();
         SubLevelEntityCollision.FirstCollisionInfo collision = entry.getValue();

         if (!collision.horizontal()) {
            continue;
         }
         if (subLevel == info.preTrackingSubLevel) {
            continue;
         }

         Vector3d mobVel = new Vector3d(
            info.preDeltaMovement.x * BLOCKS_PER_TICK_TO_METERS_PER_SECOND,
            info.preDeltaMovement.y * BLOCKS_PER_TICK_TO_METERS_PER_SECOND,
            info.preDeltaMovement.z * BLOCKS_PER_TICK_TO_METERS_PER_SECOND
         );

         Vector3d subVel = Sable.HELPER.getVelocity(level, subLevel, collision.localLocation(), new Vector3d());
         double magnitude = collision.globalDirection().dot(subVel.sub(mobVel));
         if (magnitude < impact.minSpeed()) {
            continue;
         }

         Vector3d launchVel = new Vector3d(collision.globalDirection()).mul(magnitude * impact.launchMultiplier());
         Vector3d launched = ReactionMobLauncher.launchSilent(level, mob, gameTime, launchVel);
         if (launched == null) {
            return;
         }

         playImpactSounds(level, mob);

         if (ReactionSettings.general().debug().logging()) {
            RagdollReactions.LOGGER.info(
               "[ragdoll_reactions] mob {} sublevel impact speed={} m/s launch={} m/s",
               BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()),
               ReactionLauncher.fmt(magnitude),
               ReactionLauncher.fmtVec(launched)
            );
         }
         return;
      }
   }

   private static void playImpactSounds(ServerLevel level, Mob mob) {
      if (!ReactionSettings.general().sound().enabled()) {
         return;
      }
      float volume = (float) ReactionSettings.general().sound().volume();
      float pitch = 0.9f + level.getRandom().nextFloat() * 0.2f;

      try {
         Method getHurtSound = LivingEntity.class.getDeclaredMethod("getHurtSound", DamageSource.class);
         getHurtSound.setAccessible(true);
         mob.makeSound((net.minecraft.sounds.SoundEvent) getHurtSound.invoke(mob, level.damageSources().generic()));
      } catch (ReflectiveOperationException ignored) {
      }

      level.playSound(null, mob.getX(), mob.getY(), mob.getZ(),
         SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.NEUTRAL, volume, pitch);
   }
}
