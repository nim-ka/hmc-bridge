package com.hmcbridge;

import com.hmcbridge.HmcBridge;

import net.dv8tion.jda.api.utils.MarkdownSanitizer;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessage;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;

public class DiscordSender {
	private WebhookClient client;

	public DiscordSender(String webhookId, String webhookToken) {
		client = WebhookClient.withUrl("https://discord.com/api/webhooks/" + webhookId + "/" + webhookToken);
	}

	public void sendChatMessage(String text, String username) {
		HmcBridge.log("Sending Discord message: \"" + text + (username != null ? "\" (username: " + username + ")" : "\""));

		client.send(getMessageBuilder(username)
			.setContent(MarkdownSanitizer.escape(text))
			.build());
	}

	public void sendServerMessage(String text, String username) {
		HmcBridge.log("Sending Discord message: \"" + text + (username != null ? "\" (username: " + username + ")" : "\""));

		WebhookEmbedBuilder embedBuilder = new WebhookEmbedBuilder();
		if (username != null) {
			embedBuilder.setAuthor(new WebhookEmbed.EmbedAuthor(text, createAvatarUrl(username), null));
		} else {
			embedBuilder.setTitle(new WebhookEmbed.EmbedTitle(text, null));
		}

		client.send(getMessageBuilder(null)
			.addEmbeds(embedBuilder.build())
			.build());
	}

	private static String createAvatarUrl(String id) {
		return "https://mc-heads.net/avatar/" + id + ".png";
	}

	private static WebhookMessageBuilder getMessageBuilder(String username) {
		WebhookMessageBuilder messageBuilder = new WebhookMessageBuilder();
		if (username != null) {
			messageBuilder.setUsername(username);
			messageBuilder.setAvatarUrl(createAvatarUrl(username));
		} else {
			messageBuilder.setUsername("Hazy Maze Cave");
			messageBuilder.setAvatarUrl("https://nim.cx/assets/images/h.png");
		}

		return messageBuilder;
	}
}
