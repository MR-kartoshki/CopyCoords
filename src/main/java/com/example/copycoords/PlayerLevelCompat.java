package com.example.copycoords;

import java.lang.reflect.Method;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

final class PlayerLevelCompat {
    private PlayerLevelCompat() {
    }

    static boolean isInDimension(Player player, Object dimensionKey) {
        Object playerDimension = getDimensionKey(player);
        return playerDimension != null && playerDimension.equals(dimensionKey);
    }

    static String getDimensionId(Player player) {
        Object playerDimension = getDimensionKey(player);
        return playerDimension == null ? "unknown" : playerDimension.toString();
    }

    private static Object getDimensionKey(Player player) {
        Level level = getLevel(player);
        return level == null ? null : level.dimension();
    }

    private static Level getLevel(Player player) {
        if (player == null) {
            return null;
        }

        Object level = invoke(player, "level");
        if (level instanceof Level castLevel) {
            return castLevel;
        }

        level = invoke(player, "getLevel");
        if (level instanceof Level castLevel) {
            return castLevel;
        }

        return null;
    }

    private static Object invoke(Player player, String methodName) {
        try {
            Method method = player.getClass().getMethod(methodName);
            return method.invoke(player);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }
}
