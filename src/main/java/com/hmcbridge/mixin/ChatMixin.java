package com.hmcbridge.mixin;

import com.hmcbridge.HmcBridge;

import java.util.UUID;

import net.minecraft.network.MessageType;
import net.minecraft.server.PlayerManager;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.UserCache;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class ChatMixin {
	@Inject(at = @At("RETURN"), method = "broadcastChatMessage")
	private void onChatMessage(Text message, MessageType type, UUID sender, CallbackInfo ci) {
		PlayerManager self = (PlayerManager) (Object) this;

		// i think this only happens from our own DiscordReceiver-sent chat messages?
		if (!(message instanceof TranslatableText)) {
			return;
		}

		UserCache userCache = self.getServer().getUserCache();

		TranslatableText content = (TranslatableText) message;
		String key = content.getKey();
		Object[] args = content.getArgs();

		switch (key) {
			// if this is a regular message, use the username for the webhook and strip it from the body
			case "chat.type.text":
				HmcBridge.SENDER.sendRegularMessage(argToString(args[1]), userCache.getByUuid(sender).getName());
				break;

			// for /me and /say we want to send it with the webhook's icon and name, not the user's
			case "chat.type.announcement":
			case "chat.type.emote":
				HmcBridge.SENDER.sendRegularMessage(message.getString(), null);
				break;

			// e.g. join/leave messages, death messages, achievements
			default:
				HmcBridge.SENDER.sendEmbedMessage(message.getString(), argToString(args[0]));
		}

		HmcBridge.log(message);
	}

	private static String argToString(Object arg) {
		if (arg instanceof String) {
			return (String) arg;
		}

		if (arg instanceof Text) {
			return ((Text) arg).getString();
		}

		return arg.toString();
	}
}
