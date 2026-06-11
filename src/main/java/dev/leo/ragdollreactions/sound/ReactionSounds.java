package dev.leo.ragdollreactions.sound;

import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ReactionSounds {
   private static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, "ragdoll_reactions");

   public static final Supplier<SoundEvent> PUNCH = SOUND_EVENTS.register(
      "punch", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("ragdoll_reactions", "punch"))
   );

   private ReactionSounds() {
   }

   public static void register(IEventBus modBus) {
      SOUND_EVENTS.register(modBus);
   }
}
