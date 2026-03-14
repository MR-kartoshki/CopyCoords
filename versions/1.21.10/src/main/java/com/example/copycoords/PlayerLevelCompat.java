package com.example.copycoords;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

final class PlayerLevelCompat {
    private PlayerLevelCompat() {
    }

    // Checks whether the player is currently in the requested dimension key.
    static boolean isInDimension(Player player, Object dimensionKey) {
        Object playerDimension = getDimensionKey(player);
        return playerDimension != null && playerDimension.equals(dimensionKey);
    }

    // Returns a stable dimension identifier string used in messages and templates.
    static String getDimensionId(Player player) {
        Object playerDimension = getDimensionKey(player);
        return playerDimension == null ? "unknown" : playerDimension.toString();
    }

    private static Object getDimensionKey(Player player) {
        // Uses the 1.21.10 typed world accessor.
        Level level = player == null ? null : player.level();
        return level == null ? null : level.dimension();
    }
}
