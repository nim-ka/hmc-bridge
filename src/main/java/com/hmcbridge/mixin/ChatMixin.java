package com.hmcbridge.mixin;

import com.hmcbridge.HmcBridge;

import java.util.UUID;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.MessageType;
import net.minecraft.server.PlayerManager;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class ChatMixin {
	@Inject(at = @At("TAIL"), method = "broadcastChatMessage")
	private void onChatMessage(Text message, MessageType type, UUID sender, CallbackInfo ci) {
		PlayerManager self = (PlayerManager) (Object) this;

		// i think this only happens from our own DiscordReceiver-sent chat messages?
		if (!(message instanceof TranslatableText)) {
			return;
		}

		TranslatableText content = (TranslatableText) message;
		String key = content.getKey();
		Object[] args = content.getArgs();

		String maybeUsername = args.length > 0 ? argToString(args[0]) : null;

		PlayerEntity player = self.getPlayer(sender);
		if (player == null) {
			player = self.getPlayer(maybeUsername);
		}

		String username = player != null ? player.getDisplayName().getString() : null;

		if (type == MessageType.CHAT) {
			// if this is a regular message, use the username for the webhook and strip it from the body
			// for /me and /say we want it to look like a server message, not from that user
			if (key == "chat.type.text") {
				HmcBridge.SENDER.sendChatMessage(argToString(args[1]), username);
			} else {
				HmcBridge.SENDER.sendChatMessage(message.getString(), null);
			}
		} else if (key == "multiplayer.player.joined" || key == "multiplayer.player.left") {
			// if player might not be in the PlayerManager map yet, don't use that lookup
			HmcBridge.SENDER.sendServerMessage(message.getString(), maybeUsername);
		} else {
			// e.g. death messages, achievements
			HmcBridge.SENDER.sendServerMessage(message.getString(), username);
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
