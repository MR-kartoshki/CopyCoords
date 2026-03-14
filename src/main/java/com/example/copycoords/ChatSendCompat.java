package com.example.copycoords;

import java.lang.reflect.Method;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

final class ChatSendCompat {
    private static final int INVOCATION_NO_METHOD = 0;
    private static final int INVOCATION_SUCCESS = 1;
    private static final int INVOCATION_FAILED = -1;

    private static String lastFailureReason = "unknown";

    private ChatSendCompat() {
    }

    static String getLastFailureReason() {
        return lastFailureReason;
    }

    static boolean sendChat(Minecraft client, ClientPacketListener connection, String line) {
        if (line == null || line.isBlank()) {
            lastFailureReason = "chat line is blank";
            return false;
        }

        LocalPlayer player = client == null ? null : client.player;
        Component preview = Component.literal(line);

        boolean hadCompatibleMethod = false;
        int status = invokeVoid(connection, "sendChat", new Class<?>[] { String.class }, line);
        if (status == INVOCATION_SUCCESS) {
            return true;
        }
        hadCompatibleMethod |= status != INVOCATION_NO_METHOD;

        status = invokeVoid(connection, "sendChatMessage", new Class<?>[] { String.class }, line);
        if (status == INVOCATION_SUCCESS) {
            return true;
        }
        hadCompatibleMethod |= status != INVOCATION_NO_METHOD;

        status = invokeVoid(player, "chat", new Class<?>[] { String.class }, line);
        if (status == INVOCATION_SUCCESS) {
            return true;
        }
        hadCompatibleMethod |= status != INVOCATION_NO_METHOD;

        status = invokeVoid(player, "sendChatMessage", new Class<?>[] { String.class }, line);
        if (status == INVOCATION_SUCCESS) {
            return true;
        }
        hadCompatibleMethod |= status != INVOCATION_NO_METHOD;

        status = invokeVoid(player, "chatSigned", new Class<?>[] { String.class, Component.class }, line, preview);
        if (status == INVOCATION_SUCCESS) {
            return true;
        }
        hadCompatibleMethod |= status != INVOCATION_NO_METHOD;

        status = invokeVoid(player, "chat", new Class<?>[] { String.class, Component.class }, line, preview);
        if (status == INVOCATION_SUCCESS) {
            return true;
        }
        hadCompatibleMethod |= status != INVOCATION_NO_METHOD;

        status = invokeVoid(connection, "sendChat", new Class<?>[] { String.class, Component.class }, line, preview);
        if (status == INVOCATION_SUCCESS) {
            return true;
        }
        hadCompatibleMethod |= status != INVOCATION_NO_METHOD;

        status = invokeVoid(connection, "sendChatMessage", new Class<?>[] { String.class, Component.class }, line, preview);
        if (status == INVOCATION_SUCCESS) {
            return true;
        }
        hadCompatibleMethod |= status != INVOCATION_NO_METHOD;

        if (!hadCompatibleMethod) {
            lastFailureReason = "no compatible chat send method found";
        }
        return false;
    }

    static boolean sendCommand(Minecraft client, ClientPacketListener connection, String command) {
        if (command == null || command.isBlank()) {
            lastFailureReason = "command is blank";
            return false;
        }

        LocalPlayer player = client == null ? null : client.player;
        Component preview = Component.literal(command);

        boolean hadCompatibleMethod = false;
        int status = invokeVoid(connection, "sendCommand", new Class<?>[] { String.class }, command);
        if (status == INVOCATION_SUCCESS) {
            return true;
        }
        hadCompatibleMethod |= status != INVOCATION_NO_METHOD;

        status = invokeVoid(connection, "sendCommandMessage", new Class<?>[] { String.class }, command);
        if (status == INVOCATION_SUCCESS) {
            return true;
        }
        hadCompatibleMethod |= status != INVOCATION_NO_METHOD;

        status = invokeVoid(player, "command", new Class<?>[] { String.class }, command);
        if (status == INVOCATION_SUCCESS) {
            return true;
        }
        hadCompatibleMethod |= status != INVOCATION_NO_METHOD;

        status = invokeVoid(player, "sendCommand", new Class<?>[] { String.class }, command);
        if (status == INVOCATION_SUCCESS) {
            return true;
        }
        hadCompatibleMethod |= status != INVOCATION_NO_METHOD;

        status = invokeVoid(player, "commandSigned", new Class<?>[] { String.class, Component.class }, command, preview);
        if (status == INVOCATION_SUCCESS) {
            return true;
        }
        hadCompatibleMethod |= status != INVOCATION_NO_METHOD;

        status = invokeVoid(connection, "sendCommand", new Class<?>[] { String.class, Component.class }, command, preview);
        if (status == INVOCATION_SUCCESS) {
            return true;
        }
        hadCompatibleMethod |= status != INVOCATION_NO_METHOD;

        status = invokeVoid(connection, "sendCommandMessage", new Class<?>[] { String.class, Component.class }, command, preview);
        if (status == INVOCATION_SUCCESS) {
            return true;
        }
        hadCompatibleMethod |= status != INVOCATION_NO_METHOD;

        status = invokeBoolean(player, "commandUnsigned", new Class<?>[] { String.class }, command);
        if (status == INVOCATION_SUCCESS) {
            return true;
        }
        hadCompatibleMethod |= status != INVOCATION_NO_METHOD;

        if (!hadCompatibleMethod) {
            lastFailureReason = "no compatible command send method found";
        }
        return false;
    }

    private static int invokeVoid(Object target, String methodName, Class<?>[] parameterTypes, Object... args) {
        Method method = findMethod(target, methodName, parameterTypes);
        if (method == null) {
            return INVOCATION_NO_METHOD;
        }

        try {
            method.invoke(target, args);
            return INVOCATION_SUCCESS;
        } catch (ReflectiveOperationException error) {
            lastFailureReason = methodName + " invocation failed: " + error.getClass().getSimpleName();
            return INVOCATION_FAILED;
        }
    }

    private static int invokeBoolean(Object target, String methodName, Class<?>[] parameterTypes, Object... args) {
        Method method = findMethod(target, methodName, parameterTypes);
        if (method == null) {
            return INVOCATION_NO_METHOD;
        }

        try {
            Object result = method.invoke(target, args);
            return result instanceof Boolean && (Boolean) result ? INVOCATION_SUCCESS : INVOCATION_FAILED;
        } catch (ReflectiveOperationException error) {
            lastFailureReason = methodName + " invocation failed: " + error.getClass().getSimpleName();
            return INVOCATION_FAILED;
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
