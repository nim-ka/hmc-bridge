package com.hmcbridge;

import com.hmcbridge.HmcBridge;

import com.mojang.authlib.GameProfile;

import net.minecraft.network.MessageType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.WhitelistEntry;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class DiscordReceiver extends ListenerAdapter {
	private static final int MAX_MESSAGE_LENGTH = 256;

	private String webhookId;
	private String channelId;

	private PlayerManager playerManager;

	public DiscordReceiver(String webhookId, String channelId, String token) {
		this.webhookId = webhookId;
		this.channelId = channelId;

		JDABuilder
			.createDefault(token)
			.enableIntents(
				GatewayIntent.GUILD_MEMBERS,
				GatewayIntent.MESSAGE_CONTENT)
			.addEventListeners(this)
			.build()
			.upsertCommand(
				Commands.slash("whitelist", "Add a Minecraft player to the server whitelist.")
					.addOption(OptionType.STRING, "username", "Username of player to whitelist", true))
			.queue();
	}

	public void setPlayerManager(PlayerManager playerManager) {
		this.playerManager = playerManager;
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (playerManager == null) {
			return;
		}

		if (event.getAuthor().getId().equals(webhookId)) {
			return;
		}

		Message message = event.getMessage();

		if (!message.getChannelId().equals(channelId)) {
			return;
		}

		postMessage(message);
	}

	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		if (!event.getName().equals("whitelist")) {
			return;
		}

		event.deferReply(true).queue();

		if (playerManager == null) {
			event.getHook().editOriginal("\u274c  Server is not started.").queue();
			return;
		}

		String username = event.getOption("username").getAsString();

		GameProfile player = playerManager.getServer().getUserCache().findByName(username);
		if (player == null) {
			event.getHook().editOriginal("\u274c  Could not find a Minecraft profile with that username.").queue();
			return;
		}

		playerManager.getWhitelist().add(new WhitelistEntry(player));

		event.getHook().editOriginal("\u2705  Added **" + username + "** to the server whitelist!").queue();

		HmcBridge.log("Whitelisted " + username);
	}

	private Text getUsernameText(Message message) {
		String username = message.getAuthor().getName();
		// webhooks don't have a Member
		String displayName = message.isWebhookMessage() ? username : message.getMember().getEffectiveName();

		Text usernameHoverText =
			message.isWebhookMessage() ?
				new LiteralText("APP").formatted(Formatting.AQUA) :
				new LiteralText("")
					.append(new LiteralText("Username: ").formatted(Formatting.GRAY))
					.append(new LiteralText(username));

		return
			new LiteralText(displayName)
				.styled(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, usernameHoverText)));
	}

	private void postMessage(Message message, String replyUrl) {
		// don't nest replies
		// for some reason forwards show up like replies?
		if (replyUrl == null && message.getMessageReference() != null && message.getMessageSnapshots().isEmpty()) {
			postMessage(message.getReferencedMessage(), message.getJumpUrl());
		}

		Text usernameText =
			message != null ?
				new LiteralText("")
					.append(new LiteralText("<"))
					.append(getUsernameText(message))
					.append(new LiteralText("> ")) :
				new LiteralText("");

		String content = message != null ? message.getContentDisplay() : "";

		String warning = "";

		if (content.trim().isEmpty()) {
			content = "";
			warning = "[Click to view this message on Discord]";
		}

		// any more specific reason than "empty message" gets overridden
		if (message == null) {
			// should only happen if discord hasn't resolved the reply parent
			warning = "[Click to view this reply on Discord]";
		} else if (content.length() > MAX_MESSAGE_LENGTH) {
			content = content.substring(0, MAX_MESSAGE_LENGTH - 3) + "...";
			warning = "[Click to view the rest of this message on Discord]";
		} else if (
			!message.getAttachments().isEmpty() ||
			!message.getEmbeds().isEmpty() ||
			!message.getStickers().isEmpty()) {
			warning = "[Click to view attachments on Discord]";
		} else if (!message.getMessageSnapshots().isEmpty()) {
			warning = "[Click to view forwarded message on Discord]";
		} else if (message.getPoll() != null) {
			warning = "[Click to view poll on Discord]";
		}

		Text warningText =
			new LiteralText(content.isEmpty() ? warning : " " + warning)
				.formatted(new Formatting[] { Formatting.GRAY, Formatting.ITALIC });

		Text hoverText = new LiteralText("Click to open in Discord");

		String url = message != null ? message.getJumpUrl() : replyUrl;

		Text messageText =
			new LiteralText(content)
				.append(warningText)
				.styled(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText)))
				.styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url)));

		Text text =
			new LiteralText("")
				.append(new LiteralText(replyUrl != null ? "[Discord] \u250c " : "[Discord] ").formatted(Formatting.GRAY))
				.append(usernameText)
				.append(messageText)
				.formatted(replyUrl != null ? new Formatting[] { Formatting.GRAY, Formatting.ITALIC } : new Formatting[] {});

		playerManager.broadcastChatMessage(text, MessageType.SYSTEM, Util.NIL_UUID);
	}

	private void postMessage(Message message) {
		postMessage(message, null);
	}
}
