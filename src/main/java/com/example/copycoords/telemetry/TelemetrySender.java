// shoutout to offsetmonkey538 for the original telemetry code which the telemetry part of the mod is based on
// he da goat frfr

package com.example.copycoords.telemetry;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

public final class TelemetrySender {
    private TelemetrySender() {
    }

    // update this if you want a different user agent header
    private static final String USER_AGENT = "CopyCoords/telemetry";

    /**
     * Sends the given JSON payload to the specified telemetry endpoint.
     * This method blocks until a response is received.  It throws an
     * exception on network errors or non‑2xx responses so callers can handle
     * logging or retry logic themselves.
     */
    public static void send(final URI telemetryEndpoint, final JsonObject jsonData)
            throws IOException, InterruptedException {
        byte[] data = jsonData.toString().getBytes(StandardCharsets.UTF_8);

        final HttpRequest.Builder request = HttpRequest.newBuilder(telemetryEndpoint);
        request.POST(HttpRequest.BodyPublishers.ofByteArray(data));
        request.version(HttpClient.Version.HTTP_1_1);
        request.header("User-Agent", USER_AGENT);
        request.header("Content-Type", "application/json");

        try (final HttpClient client = HttpClient.newBuilder().build()) {
            final HttpResponse<String> response = client.send(request.build(),
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() <= 299) return;

            throw new IOException("Non-2xx response: " + response.statusCode() + " body: " + response.body());
        }
    }

    // helpers for building the standard analytics JSON payloads

    public static JsonObject createAnalyticsJson(final String minecraftVersion,
                                                 final boolean isDedicatedServer,
                                                 final String gameBrand,
                                                 final String[] mods) {
        JsonArray modsArray = new JsonArray(mods.length);
        for (String mod : mods) {
            modsArray.add(mod);
        }
        return createAnalyticsJson(minecraftVersion, isDedicatedServer, gameBrand, modsArray);
    }

    public static JsonObject createAnalyticsJson(final String minecraftVersion,
                                                 final boolean isDedicatedServer,
                                                 final String gameBrand,
                                                 final Collection<String> mods) {
        JsonArray modsArray = new JsonArray(mods.size());
        for (String mod : mods) {
            modsArray.add(mod);
        }
        return createAnalyticsJson(minecraftVersion, isDedicatedServer, gameBrand, modsArray);
    }

    public static JsonObject createAnalyticsJson(final String minecraftVersion,
                                                 final boolean isDedicatedServer,
                                                 final String gameBrand,
                                                 final JsonArray mods) {
        JsonObject jsonData = new JsonObject();
        jsonData.addProperty("mc", minecraftVersion);
        jsonData.addProperty("e", isDedicatedServer ? "s" : "c");
        jsonData.addProperty("l", gameBrand);
        jsonData.add("m", mods);
        return jsonData;
    }
}
