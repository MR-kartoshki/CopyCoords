package com.example.copycoords;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;

final class ChatEventFactory {
    private ChatEventFactory() {
    }

    static ClickEvent runCommand(String command) {
        return new ClickEvent(ClickEvent.Action.RUN_COMMAND, command);
    }

    static ClickEvent copyToClipboard(String value) {
        return new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, value);
    }

    static ClickEvent suggestCommand(String command) {
        return new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command);
    }

    static HoverEvent showText(Component text) {
        return new HoverEvent(HoverEvent.Action.SHOW_TEXT, text);
    }

    static ClickEvent openUrl(String url) {
        return new ClickEvent(ClickEvent.Action.OPEN_URL, url);
    }
}
