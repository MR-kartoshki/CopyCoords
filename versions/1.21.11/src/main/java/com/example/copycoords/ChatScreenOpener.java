package com.example.copycoords;

import net.minecraft.client.Minecraft;

final class ChatScreenOpener {
    private ChatScreenOpener() {
    }

    static void open(Minecraft mc, String text) {
        mc.setScreen(new net.minecraft.client.gui.screens.ChatScreen(text, false));
    }
}

