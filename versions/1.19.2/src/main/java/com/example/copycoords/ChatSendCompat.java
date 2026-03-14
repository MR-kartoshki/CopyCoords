package com.example.copycoords;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

final class ChatSendCompat {
    private static String lastFailureReason = "unknown";

    private ChatSendCompat() {
    }

    static String getLastFailureReason() {
        return lastFailureReason;
    }

    // Sends a plain chat message using the signed local player API in 1.19.2.
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
            player.chatSigned(line, Component.literal(line));
            return true;
        } catch (Throwable error) {
            lastFailureReason = "chat failed: " + error.getClass().getSimpleName();
            return false;
        }
    }

    // Sends a slash command using unsigned command dispatch in 1.19.2.
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
            if (player.commandUnsigned(command)) {
                return true;
            }
            lastFailureReason = "commandUnsigned returned false";
            return false;
        } catch (Throwable error) {
            lastFailureReason = "command failed: " + error.getClass().getSimpleName();
            return false;
        }
    }
}
