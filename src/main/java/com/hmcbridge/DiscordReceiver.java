package com.hmcbridge;

import com.hmcbridge.HmcBridge;

import com.mojang.authlib.GameProfile;

import net.minecraft.network.MessageType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.WhitelistEntry;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
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

		if (event.isWebhookMessage()) {
			return;
		}

		Message message = event.getMessage();
		if (!message.getChannelId().equals(channelId)) {
			return;
		}

		String content = message.getContentRaw();

		Text text = new LiteralText("")
			.append(new LiteralText("[Discord] ").formatted(Formatting.GRAY))
			.append(new LiteralText("<"))
			.append(getMemberText(message.getMember()))
			.append(new LiteralText("> "))
			.append(new LiteralText(content));

		playerManager.broadcastChatMessage(text, MessageType.SYSTEM, Util.NIL_UUID);
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

	private Text getMemberText(Member member) {
		Text nicknameText = new LiteralText("")
			.append(new LiteralText("Nickname: ").formatted(Formatting.GRAY))
			.append(new LiteralText(member.getEffectiveName()));

		return new LiteralText(member.getUser().getName())
			.styled(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, nicknameText)));
	}
}
