package quek.undergarden.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import quek.undergarden.registry.UGSoundEvents;

public class UndergardenClient {

	public static void playPortalSound() {
		Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forLocalAmbience(UGSoundEvents.UNDERGARDEN_PORTAL_TRAVEL.get(), 1.0F, 1.0F));
	}

	@Nullable
	public static RegistryAccess registryAccess() {
		if (Minecraft.getInstance().level != null) {
			return Minecraft.getInstance().level.registryAccess();
		}
		return null;
	}
}
