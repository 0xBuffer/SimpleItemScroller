package com.simpleitemscroller.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

public final class Config {
    public static boolean enabled = true;
    public static int delayMs = 0;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("simple-item-scroller.json");

    private Config() {
    }

    public static void load() {
        try {
            if (Files.exists(FILE)) {
                Data data = GSON.fromJson(Files.readString(FILE), Data.class);
                if (data != null) {
                    enabled = data.enabled;
                    delayMs = clamp(data.delayMs);
                }
            }
        } catch (Exception ignored) {
        }
    }

    public static void save() {
        try {
            Data data = new Data();
            data.enabled = enabled;
            data.delayMs = delayMs;
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(data));
        } catch (Exception ignored) {
        }
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(1000, value));
    }

    private static class Data {
        public boolean enabled = true;
        public int delayMs = 0;
    }
}
