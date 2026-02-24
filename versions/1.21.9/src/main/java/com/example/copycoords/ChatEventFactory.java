package com.example.copycoords;

import java.net.URI;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;

final class ChatEventFactory {
    private ChatEventFactory() {
    }

    static ClickEvent runCommand(String command) {
        return new ClickEvent.RunCommand(command);
    }

    static ClickEvent copyToClipboard(String value) {
        return new ClickEvent.CopyToClipboard(value);
    }

    static HoverEvent showText(Component text) {
        return new HoverEvent.ShowText(text);
    }

    static ClickEvent openUrl(String url) {
        return new ClickEvent.OpenUrl(URI.create(url));
    }
}

