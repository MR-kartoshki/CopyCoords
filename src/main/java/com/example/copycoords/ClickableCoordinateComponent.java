package com.example.copycoords;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.HoverEvent;

public class ClickableCoordinateComponent {
    
    /**
     * Creates a clickable coordinate component
     * @param coordString The coordinate string to display
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @param dimensionId The dimension ID
     * @return A Component with click action to re-copy coordinates
     */
    public static MutableComponent createClickableCoordinate(String coordString, int x, int y, int z, String dimensionId) {
        MutableComponent coord = Component.literal(coordString);

        ClickEvent clickEvent = buildClickEvent(coordString);
        HoverEvent hoverEvent = buildHoverEvent(Component.literal("Click to copy coordinates"));

        Style style = Style.EMPTY;
        if (clickEvent != null) {
            style = style.withClickEvent(clickEvent);
        }
        if (hoverEvent != null) {
            style = style.withHoverEvent(hoverEvent);
        }
        
        return coord.withStyle(style);
    }

    private static ClickEvent buildClickEvent(String coordString) {
        return ChatEventFactory.copyToClipboard(coordString);
    }

    private static HoverEvent buildHoverEvent(Component text) {
        return ChatEventFactory.showText(text);
    }

    /**
     * Creates a message with clickable coordinates
     * @param prefix The prefix text (e.g., "Your current coordinates are: ")
     * @param coordString The coordinate string
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @param dimensionId The dimension ID
     * @return A Component combining prefix and clickable coordinates
     */
    public static MutableComponent createClickableCoordinateMessage(String prefix, String coordString, int x, int y, int z, String dimensionId) {
        MutableComponent message = Component.literal(prefix);
        MutableComponent clickable = createClickableCoordinate(coordString, x, y, z, dimensionId);
        return message.append(clickable);
    }
}

