package com.example.tabmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

public class TabMod implements ModInitializer {

    private static TabUpdater updater;

    @Override
    public void onInitialize() {
        System.out.println("[TabMod] Initializing (server-only, minimal style)");

        updater = new TabUpdater();

        // load config and whitelist on server start
        ServerLifecycleEvents.SERVER_STARTED.register((MinecraftServer server) -> {
            updater.onServerStart(server);
        });

        // periodic update (runs on server thread)
        ServerTickEvents.END_SERVER_TICK.register(server -> updater.onTick(server));
    }
}