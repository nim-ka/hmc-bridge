package com.hmcbridge;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.minecraft.network.MessageType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class HmcBridge implements ModInitializer {
	public static final String MOD_ID = "hmc-bridge";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	public static int tickCount = 0;

	@Override
	public void onInitialize() {
		ServerLifecycleEvents.SERVER_STARTED.register(HmcBridge::onServerStarted);
		ServerLifecycleEvents.SERVER_STOPPED.register(HmcBridge::onServerStopped);

		ServerTickEvents.END_SERVER_TICK.register(HmcBridge::onEndTick);

//		LOGGER.info("hmc-bridge: Server started");
/*
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			LOGGER.info("hmc-bridge: Server stopped");
		}));
*/
	}

	public static void onServerStarted(MinecraftServer server) {
		LOGGER.info("hmc-bridge: Server started");
	}

	public static void onServerStopped(MinecraftServer server) {
		LOGGER.info("hmc-bridge: Server stopped");
	}

	public static void onEndTick(MinecraftServer server) {
		tickCount++;
		if (tickCount % 50 == 0) {
			server.getPlayerManager().broadcastChatMessage(new LiteralText("test"), MessageType.SYSTEM, Util.NIL_UUID);
		}
	}

	public static Identifier id(String path) {
		return new Identifier(MOD_ID, path);
	}
}
