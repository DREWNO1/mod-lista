package com.example.tabmod;

import com.google.gson.*;
import net.minecraft.network.packet.s2c.play.PlayerListHeaderS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.stat.Stats;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class TabUpdater {

    private final List<String> whitelist = new ArrayList<>();
    private JsonObject config = new JsonObject();
    private int tickCounter = 0;
    private int updateInterval = 40; // default ticks

    public void onServerStart(MinecraftServer server) {
        System.out.println("[TabMod] Server started — loading config and whitelist");
        loadConfig(server);
        loadWhitelist(server);
    }

    private void loadConfig(MinecraftServer server) {
        try {
            Path cfg = server.getRunDirectory().toPath().resolve("tabmod_config.json");
            if (!Files.exists(cfg)) {
                Files.copy(getClass().getResourceAsStream("/tabmod_config.json"), cfg);
            }
            String raw = Files.readString(cfg);
            config = JsonParser.parseString(raw).getAsJsonObject();
            updateInterval = config.has("update_interval_ticks")
                    ? config.get("update_interval_ticks").getAsInt()
                    : 40;

            System.out.println("[TabMod] Config loaded (interval: " + updateInterval + " ticks)");
        } catch (IOException e) {
            System.out.println("[TabMod] Failed to load config: " + e.getMessage());
        }
    }

    private void loadWhitelist(MinecraftServer server) {
        try {
            Path wl = server.getRunDirectory().toPath().resolve("whitelist.json");
            if (!Files.exists(wl)) {
                System.out.println("[TabMod] whitelist.json not found.");
                return;
            }

            String raw = Files.readString(wl);
            JsonArray arr = JsonParser.parseString(raw).getAsJsonArray();
            whitelist.clear();

            for (JsonElement el : arr) {
                if (el.isJsonObject() && el.getAsJsonObject().has("name")) {
                    whitelist.add(el.getAsJsonObject().get("name").getAsString());
                }
            }

            System.out.println("[TabMod] Whitelist loaded: " + whitelist);
        } catch (Exception e) {
            System.out.println("[TabMod] Error reading whitelist.json: " + e.getMessage());
        }
    }

    public void onTick(MinecraftServer server) {
        tickCounter++;
        if (tickCounter < updateInterval) return;
        tickCounter = 0;

        Text header = Text.literal("");
        Text footer = Text.literal("");

        try {
            if (config.has("header")) {
                StringBuilder sb = new StringBuilder();
                for (JsonElement s : config.getAsJsonArray("header")) {
                    sb.append(s.getAsString()).append("\n");
                }
                header = Text.literal(Formatting.WHITE + sb.toString());
            }

            if (config.has("footer")) {
                StringBuilder sb = new StringBuilder();
                for (JsonElement s : config.getAsJsonArray("footer")) {
                    sb.append(s.getAsString()).append("\n");
                }
                footer = Text.literal(Formatting.GRAY + sb.toString());
            }
        } catch (Exception ignored) {}

        for (ServerPlayerEntity viewer : server.getPlayerManager().getPlayerList()) {

            for (String name : whitelist) {
                ServerPlayerEntity sp = server.getPlayerManager().getPlayer(name);

                boolean online = sp != null;
                String icon = online ? "✔" : "✖";

                int deaths = 0;
                long playTicks = 0;
                int ping = 0;
                String lastSeen = "offline";

                if (online) {
                    try {
                        deaths = sp.getStatHandler().getStat(Stats.CUSTOM, Stats.DEATHS);
                        playTicks = sp.getStatHandler().getStat(Stats.CUSTOM, Stats.PLAY_TIME);
                        ping = sp.pingMilliseconds;
                        lastSeen = "online";
                    } catch (Exception ignored) {}
                }

                long hours = playTicks / 72000L;

                String line =
                        " §7• §f" + name +
                                " §8│ §a" + icon +
                                " §8│ §7☠ " + deaths +
                                " §8│ §7⏱ " + hours + "h" +
                                " §8│ §7📡 " + ping + "ms" +
                                " §8│ §7⏲ " + lastSeen;

                viewer.sendMessage(Text.literal(line), false);
            }

            PlayerListHeaderS2CPacket packet = new PlayerListHeaderS2CPacket(header, footer);
            viewer.networkHandler.sendPacket(packet);
        }
    }
}
