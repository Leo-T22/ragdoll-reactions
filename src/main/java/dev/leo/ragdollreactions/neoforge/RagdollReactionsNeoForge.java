package dev.leo.ragdollreactions.neoforge;

import dev.leo.ragdollreactions.RagdollReactionsBootstrap;
import dev.leo.ragdollreactions.neoforge.config.ReactionConfig;
import dev.leo.ragdollreactions.physics.ImpactReactionHandler;
import dev.leo.sableplayerragdoll.api.RagdollEndEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

@Mod("ragdoll_reactions")
public final class RagdollReactionsNeoForge {
   public RagdollReactionsNeoForge(IEventBus modBus, ModContainer modContainer) {
      modBus.addListener(ReactionConfig::onLoad);
      modBus.addListener(ReactionConfig::onReload);
      ReactionConfig.register(modContainer);
      modBus.addListener(RagdollReactionsNeoForge::onCommonSetup);
      NeoForge.EVENT_BUS.addListener(RagdollReactionsNeoForge::onRagdollEnd);
      NeoForge.EVENT_BUS.addListener(RagdollReactionsNeoForge::onServerStopped);
   }

   private static void onCommonSetup(FMLCommonSetupEvent event) {
      event.enqueueWork(RagdollReactionsBootstrap::init);
   }

   private static void onRagdollEnd(RagdollEndEvent event) {
      ImpactReactionHandler.onPlayerReleased(event.player());
   }

   private static void onServerStopped(ServerStoppedEvent event) {
      ImpactReactionHandler.resetState();
   }
}
