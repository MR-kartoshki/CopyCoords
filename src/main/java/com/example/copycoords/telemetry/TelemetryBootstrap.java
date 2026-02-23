package com.example.copycoords.telemetry;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.fabricmc.loader.api.FabricLoader;

import java.net.URI;

public final class TelemetryBootstrap {
    private static final long RATE_LIMIT_MS = 86_400_000L;
    private static final String ENDPOINT = "https://140.86.211.122.sslip.io/ingest";

    private TelemetryBootstrap() {
    }

    /**
     * Normal startup call; obeys configuration and rate‑limiting.
     */
    public static void initAndMaybeSend() {
        TelemetryConfig cfg = TelemetryConfig.loadOrCreate();
        if (!cfg.enabled) {
            System.out.println("CopyCoords: Telemetry not sent! (disabled)");
            return;
        }

        final long now = System.currentTimeMillis();
        if (now - cfg.lastSent < RATE_LIMIT_MS) {
            System.out.println("CopyCoords: Telemetry not sent! (rate limited)");
            return;
        }

        doSend(now);
    }


    private static void doSend(long now) {
        String minecraftVersion = FabricLoader.getInstance()
                .getModContainer("minecraft")
                .map(mod -> mod.getMetadata().getVersion().getFriendlyString())
                .map(v -> {
                    // drop "minecraft-" prefix if present
                    String cleaned = v;
                    if (cleaned.startsWith("minecraft-")) {
                        cleaned = cleaned.substring("minecraft-".length());
                    }
                    // cut off at first hyphen (removes mappings, flavours, etc.)
                    int idx = cleaned.indexOf('-');
                    if (idx != -1) cleaned = cleaned.substring(0, idx);
                    return cleaned;
                })
                .orElse("unknown");
        // log so we can verify in the game log which version was detected
        System.out.println("CopyCoords: detected Minecraft version " + minecraftVersion);

        JsonObject payload = TelemetrySender.createAnalyticsJson(
                minecraftVersion, false, "fabric", new String[] {"copycoords"});

        try {
            TelemetrySender.send(URI.create(ENDPOINT), payload);
            TelemetryConfig cfg = TelemetryConfig.loadOrCreate();
            cfg.lastSent = now;
            cfg.save();
            System.out.println("CopyCoords: Telemetry sent!");
        } catch (Exception e) {
            System.out.println("CopyCoords: Telemetry not sent! (" + e.getMessage() + ")");
        }
    }
}
