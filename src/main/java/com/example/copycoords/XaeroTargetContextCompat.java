package com.example.copycoords;

import net.minecraft.client.Minecraft;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

final class XaeroTargetContextCompat {
    private XaeroTargetContextCompat() {
    }

    static List<String> getCurrentProfileCandidates(Minecraft client) {
        if (client == null) {
            return List.of();
        }

        Set<String> candidates = new LinkedHashSet<>();

        Object singleplayerServer = invokeNoArg(client, "getSingleplayerServer");
        addCandidate(candidates, extractSingleplayerLevelName(singleplayerServer));

        Object serverData = invokeNoArg(client, "getCurrentServer");
        String serverName = extractStringValue(serverData, "name");
        String serverIp = extractStringValue(serverData, "ip");
        addCandidate(candidates, "Multiplayer_" + serverName);
        addCandidate(candidates, serverIp);
        addCandidate(candidates, stripPort(serverIp));

        return new ArrayList<>(candidates);
    }

    private static void addCandidate(Set<String> candidates, String value) {
        if (value == null) {
            return;
        }

        String sanitized = sanitizeFolderName(value);
        if (!sanitized.isBlank()) {
            candidates.add(sanitized);
        }
    }

    private static String extractSingleplayerLevelName(Object singleplayerServer) {
        if (singleplayerServer == null) {
            return null;
        }

        Object worldData = invokeNoArg(singleplayerServer, "getWorldData");
        String levelName = invokeStringNoArg(worldData, "getLevelName");
        if (levelName != null && !levelName.isBlank()) {
            return levelName;
        }

        levelName = invokeStringNoArg(worldData, "getLevelIdName");
        if (levelName != null && !levelName.isBlank()) {
            return levelName;
        }

        return invokeStringNoArg(singleplayerServer, "getWorldName");
    }

    private static String extractStringValue(Object target, String name) {
        if (target == null) {
            return null;
        }

        try {
            Field field = target.getClass().getField(name);
            Object value = field.get(target);
            if (value instanceof String stringValue) {
                return stringValue;
            }
        } catch (ReflectiveOperationException ignored) {
        }

        String getterName = "get" + name.substring(0, 1).toUpperCase(Locale.ROOT) + name.substring(1);
        return invokeStringNoArg(target, getterName);
    }

    private static Object invokeNoArg(Object target, String methodName) {
        if (target == null) {
            return null;
        }

        try {
            Method method = target.getClass().getMethod(methodName);
            return method.invoke(target);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static String invokeStringNoArg(Object target, String methodName) {
        Object value = invokeNoArg(target, methodName);
        return value instanceof String stringValue ? stringValue : null;
    }

    private static String stripPort(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        int separator = value.indexOf(':');
        if (separator <= 0) {
            return value;
        }
        return value.substring(0, separator);
    }

    private static String sanitizeFolderName(String value) {
        if (value == null) {
            return "";
        }

        return value.trim()
                .replaceAll("[\\\\/:*?\"<>|]", "_")
                .replaceAll("\\s+", " ");
    }
}
