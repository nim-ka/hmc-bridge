package com.hmcbridge.mixin;

import com.hmcbridge.HmcBridge;

import java.util.UUID;

import net.minecraft.network.MessageType;
import net.minecraft.server.PlayerManager;
import net.minecraft.text.Text;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class ChatMixin {
	@Inject(at = @At("TAIL"), method = "broadcastChatMessage")
	private void onChatMessage(Text message, MessageType type, UUID sender, CallbackInfo ci) {
		HmcBridge.LOGGER.info("hmc-bridge: " + message.getString());
		HmcBridge.LOGGER.info("hmc-bridge: " + message);
		HmcBridge.LOGGER.info("hmc-bridge: " + type);
		HmcBridge.LOGGER.info("hmc-bridge: " + sender);
	}
}
