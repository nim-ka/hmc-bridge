package com.hmcbridge;

import com.hmcbridge.HmcBridge;

import net.minecraft.network.MessageType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class DiscordReceiver extends ListenerAdapter {
	private String channelId;

	private PlayerManager playerManager;

	public DiscordReceiver(String channelId, String token) {
		this.channelId = channelId;

		JDABuilder
			.createDefault(token)
			.enableIntents(
				GatewayIntent.GUILD_MEMBERS,
				GatewayIntent.MESSAGE_CONTENT
			)
			.addEventListeners(this)
			.build();
	}

	public void setPlayerManager(PlayerManager playerManager) {
		this.playerManager = playerManager;
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (playerManager == null) {
			return;
		}

		if (event.isWebhookMessage()) {
			return;
		}

		Message message = event.getMessage();
		if (!message.getChannelId().equals(channelId)) {
			return;
		}

		String username = message.getAuthor().getName();
		String nickname = message.getMember().getEffectiveName();
		String content = message.getContentRaw();

		MutableText nicknameText = new LiteralText("")
			.append(new LiteralText("Nickname: ").formatted(Formatting.GRAY))
			.append(new LiteralText(nickname));

		MutableText usernameText = new LiteralText(username)
			.styled(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, nicknameText)));

		MutableText text = new LiteralText("")
			.append(new LiteralText("[Discord] ").formatted(Formatting.GRAY))
			.append(new LiteralText("<"))
			.append(usernameText)
			.append(new LiteralText("> "))
			.append(new LiteralText(content));

		playerManager.broadcastChatMessage(text, MessageType.SYSTEM, Util.NIL_UUID);
	}
}
