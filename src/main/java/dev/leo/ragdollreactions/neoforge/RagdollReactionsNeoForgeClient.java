package dev.leo.ragdollreactions.neoforge;

import dev.leo.ragdollreactions.neoforge.client.ClientMotionSampler;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = "ragdoll_reactions", dist = {Dist.CLIENT})
public final class RagdollReactionsNeoForgeClient {
   public RagdollReactionsNeoForgeClient(ModContainer container) {
      container.registerExtensionPoint(IConfigScreenFactory.class, (IConfigScreenFactory) ConfigurationScreen::new);
      ClientMotionSampler.init();
   }
}
