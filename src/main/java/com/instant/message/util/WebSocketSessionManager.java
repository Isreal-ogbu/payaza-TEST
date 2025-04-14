package com.instant.message.util;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class WebSocketSessionManager {
    private static final ConcurrentMap<String, String> connectionToUser = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, Set<String>> userToConnections = new ConcurrentHashMap<>();

    public static void addSession(String connectionId, String userId) {
        connectionToUser.put(connectionId, userId);
        userToConnections.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(connectionId);
    }

    public static void removeSession(String connectionId) {
        String userId = connectionToUser.remove(connectionId);
        if (userId != null) {
            userToConnections.computeIfPresent(userId, (k, v) -> {
                v.remove(connectionId);
                return v.isEmpty() ? null : v;
            });
        }
    }

    public static Set<String> getConnectionsForUser(String userId) {
        return userToConnections.getOrDefault(userId, Set.of());
    }

    public static String getUserForConnection(String connectionId) {
        return connectionToUser.get(connectionId);
    }
}