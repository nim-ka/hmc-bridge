package com.hmcbridge;

import com.hmcbridge.DiscordSender;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class HmcBridge implements ModInitializer {
	public static final String MOD_ID = "hmc-bridge";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	public static final DiscordSender SENDER = new DiscordSender(
		System.getenv("WEBHOOK_ID"),
		System.getenv("WEBHOOK_TOKEN")
	);

	@Override
	public void onInitialize() {
		ServerLifecycleEvents.SERVER_STARTED.register(HmcBridge::onServerStarted);

		Runtime.getRuntime().addShutdownHook(new Thread(HmcBridge::onServerStopped));
	}

	public static Identifier id(String path) {
		return new Identifier(MOD_ID, path);
	}

	public static void log(Object message) {
		LOGGER.info("[hmc-bridge] " + message.toString());
	}

	private static void onServerStarted(MinecraftServer server) {
		SENDER.sendServerMessage("\u2705  Server started", null);
	}

	private static void onServerStopped() {
		SENDER.sendServerMessage("\uD83D\uDED1  Server stopped", null);
	}
}
