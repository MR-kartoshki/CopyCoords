package com.example.copycoords;

import java.lang.reflect.Method;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

final class ChatSendCompat {
    private ChatSendCompat() {
    }

    static boolean sendChat(Minecraft client, ClientPacketListener connection, String line) {
        if (line == null || line.isBlank()) {
            return false;
        }

        LocalPlayer player = client == null ? null : client.player;
        Component preview = Component.literal(line);

        return invokeVoid(connection, "sendChat", new Class<?>[] { String.class }, line)
                || invokeVoid(player, "chat", new Class<?>[] { String.class }, line)
                || invokeVoid(player, "chatSigned", new Class<?>[] { String.class, Component.class }, line, preview)
                || invokeVoid(player, "chat", new Class<?>[] { String.class, Component.class }, line, preview)
                || invokeVoid(connection, "sendChat", new Class<?>[] { String.class, Component.class }, line, preview);
    }

    static boolean sendCommand(Minecraft client, ClientPacketListener connection, String command) {
        if (command == null || command.isBlank()) {
            return false;
        }

        LocalPlayer player = client == null ? null : client.player;
        Component preview = Component.literal(command);

        if (invokeVoid(connection, "sendCommand", new Class<?>[] { String.class }, command)
                || invokeVoid(player, "command", new Class<?>[] { String.class }, command)
                || invokeVoid(player, "commandSigned", new Class<?>[] { String.class, Component.class }, command, preview)
                || invokeVoid(connection, "sendCommand", new Class<?>[] { String.class, Component.class }, command, preview)) {
            return true;
        }

        return invokeBoolean(player, "commandUnsigned", new Class<?>[] { String.class }, command);
    }

    private static boolean invokeVoid(Object target, String methodName, Class<?>[] parameterTypes, Object... args) {
        Method method = findMethod(target, methodName, parameterTypes);
        if (method == null) {
            return false;
        }

        try {
            method.invoke(target, args);
            return true;
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    private static boolean invokeBoolean(Object target, String methodName, Class<?>[] parameterTypes, Object... args) {
        Method method = findMethod(target, methodName, parameterTypes);
        if (method == null) {
            return false;
        }

        try {
            Object result = method.invoke(target, args);
            return result instanceof Boolean && (Boolean) result;
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    private static Method findMethod(Object target, String methodName, Class<?>[] parameterTypes) {
        if (target == null) {
            return null;
        }

        Class<?> type = target.getClass();
        try {
            return type.getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException ignored) {
        }

        try {
            Method method = type.getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }
}
