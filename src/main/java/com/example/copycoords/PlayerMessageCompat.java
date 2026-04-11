package com.example.copycoords;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

final class PlayerMessageCompat {
    private PlayerMessageCompat() {
    }

    static void send(LocalPlayer player, Component message) {
        player.displayClientMessage(message, false);
    }
}
