package com.example.copycoords;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Consumer;

final class ChatReceiveCompat {
    private static final String EVENTS_CLASS_NAME = "net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents";
    private static final String EVENT_INTERFACE_CLASS_NAME = "net.fabricmc.fabric.api.event.Event";
    private static boolean registered = false;

    private ChatReceiveCompat() {
    }

    static void register(Consumer<String> handler) {
        if (registered || handler == null) {
            return;
        }

        registered = true;
        if (!registerFabricMessageEvents(handler)) {
            System.err.println("CopyCoords: incoming chat detection is unavailable on this version.");
        }
    }

    private static boolean registerFabricMessageEvents(Consumer<String> handler) {
        try {
            Class<?> eventsClass = Class.forName(EVENTS_CLASS_NAME);
            boolean chatRegistered = registerEvent(eventsClass, "CHAT", "Chat", handler, false);
            boolean gameRegistered = registerEvent(eventsClass, "GAME", "Game", handler, true);
            return chatRegistered || gameRegistered;
        } catch (ClassNotFoundException error) {
            return false;
        } catch (ReflectiveOperationException error) {
            System.err.println("CopyCoords: failed to register incoming chat detection: " + error.getMessage());
            return false;
        }
    }

    private static boolean registerEvent(Class<?> eventsClass,
                                         String fieldName,
                                         String listenerTypeName,
                                         Consumer<String> handler,
                                         boolean overlayAware) throws ReflectiveOperationException {
        Field eventField = eventsClass.getField(fieldName);
        Object event = eventField.get(null);
        if (event == null) {
            return false;
        }

        Class<?> listenerType = Class.forName(eventsClass.getName() + "$" + listenerTypeName);
        Class<?> eventInterface = Class.forName(EVENT_INTERFACE_CLASS_NAME);
        InvocationHandler invocationHandler = (proxy, method, args) -> {
            if (method.getDeclaringClass() == Object.class) {
                return switch (method.getName()) {
                    case "toString" -> "CopyCoordsChatReceiveCompatListener";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == (args == null || args.length == 0 ? null : args[0]);
                    default -> null;
                };
            }

            if (overlayAware && args != null && args.length > 1 && args[1] instanceof Boolean overlay && overlay) {
                return null;
            }

            String text = extractText(args == null || args.length == 0 ? null : args[0]);
            if (text != null && !text.isBlank()) {
                handler.accept(text);
            }
            return null;
        };

        Object listener = Proxy.newProxyInstance(listenerType.getClassLoader(), new Class<?>[]{listenerType}, invocationHandler);
        Method registerMethod = eventInterface.getMethod("register", Object.class);
        registerMethod.invoke(event, listener);
        return true;
    }

    private static String extractText(Object message) {
        if (message == null) {
            return null;
        }

        try {
            Method getString = message.getClass().getMethod("getString");
            Object value = getString.invoke(message);
            if (value instanceof String stringValue) {
                return stringValue;
            }
        } catch (ReflectiveOperationException ignored) {
        }

        return message.toString();
    }
}
