package com.example.copycoords;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CopyCoordsConfig {
    public static final int MIN_DECIMAL_PLACES = 0;
    public static final int MAX_DECIMAL_PLACES = 10;
    public static final int DEFAULT_DECIMAL_PLACES = 2;
    public static final int MIN_CHAT_DETECTIONS_PER_MESSAGE = 1;
    public static final int MAX_CHAT_DETECTIONS_PER_MESSAGE = 5;
    public static final int DEFAULT_CHAT_DETECTIONS_PER_MESSAGE = 2;

    public boolean copyToClipboard = true;
    public boolean copyConvertedToClipboard = true;
    public boolean showDimensionInCoordinates = true;
    // disabled by default to avoid unexpected chat spam
    public boolean instantChatEnabled = false;
    public boolean showInstantChatSendUnboundHint = true;
    public boolean pasteToChatInput = false;
    public boolean chatCoordinateDetectionEnabled = true;
    public int chatCoordinateDetectionMaxPerMessage = DEFAULT_CHAT_DETECTIONS_PER_MESSAGE;
    public String coordinateFormat = "space";
    public int decimalPlaces = DEFAULT_DECIMAL_PLACES;
    public String coordinateTemplate = "";
    public boolean mapLinksEnabled = false;
    public String dynmapUrlTemplate = "http://localhost:8123/?world={world}&map=flat&x={x}&y={y}&z={z}";
    public String bluemapUrlTemplate = "http://localhost:8100/#world:{world}:{x}:{y}:{z}:150:0:0:0:0:perspective";
    public String webMapUrlTemplate = "";

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(CoordinateFormat.class, (JsonDeserializer<CoordinateFormat>) (json, typeOfT, context) -> {
                String value = json.getAsString();
                return CoordinateFormat.fromId(value);
            })
            .create();

    private static Path configPath;

    private static Path getScopedConfigPath() {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        return configDir.resolve("copycoords").resolve("copycoords.json");
    }

    private static Path getLegacyConfigPath() {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        return configDir.resolve("copycoords.json");
    }

    public static CopyCoordsConfig load() {
        configPath = getScopedConfigPath();
        Path legacyPath = getLegacyConfigPath();

        Path readPath = Files.exists(configPath) ? configPath : legacyPath;
        if (Files.exists(readPath)) {
            try {
                String json = Files.readString(readPath);
                JsonObject raw = null;
                try {
                    raw = JsonParser.parseString(json).getAsJsonObject();
                } catch (Throwable ignored) {
                }

                CopyCoordsConfig config = GSON.fromJson(json, CopyCoordsConfig.class);
                if (config != null) {
                    if (raw == null || !raw.has("instantChatEnabled")) {
                        config.instantChatEnabled = false;
                    }
                    if (raw == null || !raw.has("showInstantChatSendUnboundHint")) {
                        config.showInstantChatSendUnboundHint = true;
                    }
                    if (raw == null || !raw.has("decimalPlaces")) {
                        config.decimalPlaces = DEFAULT_DECIMAL_PLACES;
                    } else {
                        config.decimalPlaces = clampDecimalPlaces(config.decimalPlaces);
                    }
                    if (raw == null || !raw.has("chatCoordinateDetectionEnabled")) {
                        config.chatCoordinateDetectionEnabled = true;
                    }
                    if (raw == null || !raw.has("chatCoordinateDetectionMaxPerMessage")) {
                        config.chatCoordinateDetectionMaxPerMessage = DEFAULT_CHAT_DETECTIONS_PER_MESSAGE;
                    } else {
                        config.chatCoordinateDetectionMaxPerMessage = clampChatCoordinateDetectionMaxPerMessage(
                                config.chatCoordinateDetectionMaxPerMessage);
                    }
                    if (!configPath.equals(readPath)) {
                        config.save();
                    }
                    return config;
                }
            } catch (IOException e) {
                System.err.println("Failed to read copycoords configuration: " + e.getMessage());
            }
        }

        CopyCoordsConfig config = new CopyCoordsConfig();
        config.save();
        return config;
    }

    public static int clampDecimalPlaces(int decimalPlaces) {
        return Math.max(MIN_DECIMAL_PLACES, Math.min(MAX_DECIMAL_PLACES, decimalPlaces));
    }

    public static int clampChatCoordinateDetectionMaxPerMessage(int maxPerMessage) {
        return Math.max(MIN_CHAT_DETECTIONS_PER_MESSAGE,
                Math.min(MAX_CHAT_DETECTIONS_PER_MESSAGE, maxPerMessage));
    }

    public void save() {
        if (configPath == null) {
            configPath = getScopedConfigPath();
        }
        try {

            Files.createDirectories(configPath.getParent());

            String json = GSON.toJson(this);
            Files.writeString(configPath, json);
        } catch (IOException e) {
            System.err.println("Failed to save copycoords configuration: " + e.getMessage());
        }
    }
}

