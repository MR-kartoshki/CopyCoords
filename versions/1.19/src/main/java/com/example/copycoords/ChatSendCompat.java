package com.example.copycoords;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;

final class ChatSendCompat {
    private static String lastFailureReason = "unknown";

    private ChatSendCompat() {
    }

    static String getLastFailureReason() {
        return lastFailureReason;
    }

    // Sends a plain chat message using the local player API in 1.19.x.
    static boolean sendChat(Minecraft client, ClientPacketListener connection, String line) {
        if (line == null || line.isBlank()) {
            lastFailureReason = "chat line is blank";
            return false;
        }

        LocalPlayer player = client == null ? null : client.player;
        if (player == null) {
            lastFailureReason = "chat player is null";
            return false;
        }

        try {
            player.chat(line);
            return true;
        } catch (Throwable error) {
            lastFailureReason = "chat failed: " + error.getClass().getSimpleName();
            return false;
        }
    }

    // Sends a slash command using the local player API in 1.19.x.
    static boolean sendCommand(Minecraft client, ClientPacketListener connection, String command) {
        if (command == null || command.isBlank()) {
            lastFailureReason = "command is blank";
            return false;
        }

        LocalPlayer player = client == null ? null : client.player;
        if (player == null) {
            lastFailureReason = "command player is null";
            return false;
        }

        try {
            player.command(command);
            return true;
        } catch (Throwable error) {
            lastFailureReason = "command failed: " + error.getClass().getSimpleName();
            return false;
        }
    }
}
