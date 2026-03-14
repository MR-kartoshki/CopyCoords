package com.example.copycoords;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;

final class ChatSendCompat {
    private static String lastFailureReason = "unknown";

    private ChatSendCompat() {
    }

    static String getLastFailureReason() {
        return lastFailureReason;
    }

    // Sends a plain chat message through the packet listener API in 1.20+.
    static boolean sendChat(Minecraft client, ClientPacketListener connection, String line) {
        if (line == null || line.isBlank()) {
            lastFailureReason = "chat line is blank";
            return false;
        }

        if (connection == null) {
            lastFailureReason = "chat connection is null";
            return false;
        }

        try {
            connection.sendChat(line);
            return true;
        } catch (Throwable error) {
            lastFailureReason = "sendChat failed: " + error.getClass().getSimpleName();
            return false;
        }
    }

    // Sends a slash command through the packet listener API in 1.20+.
    static boolean sendCommand(Minecraft client, ClientPacketListener connection, String command) {
        if (command == null || command.isBlank()) {
            lastFailureReason = "command is blank";
            return false;
        }

        if (connection == null) {
            lastFailureReason = "command connection is null";
            return false;
        }

        try {
            connection.sendCommand(command);
            return true;
        } catch (Throwable error) {
            lastFailureReason = "sendCommand failed: " + error.getClass().getSimpleName();
            return false;
        }
    }
}
