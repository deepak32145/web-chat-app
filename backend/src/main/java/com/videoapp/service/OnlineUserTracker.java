package com.videoapp.service;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OnlineUserTracker {

    // Maps both real WebSocket sessionIds and synthetic "rest-{userId}" keys to userId.
    // A user is online if their userId appears in any map value.
    private final Map<String, Long> sessionUserMap = new ConcurrentHashMap<>();

    public void addSession(String sessionId, Long userId) {
        sessionUserMap.put(sessionId, userId);
    }

    public Long removeSession(String sessionId) {
        return sessionUserMap.remove(sessionId);
    }

    // Called by REST setUserOnline — acts as a virtual session so the user stays
    // online during the WebSocket reconnect window after a page refresh.
    public void markOnline(Long userId) {
        sessionUserMap.put("rest-" + userId, userId);
    }

    // Called by REST setUserOffline / sendBeacon on page close.
    public void markOffline(Long userId) {
        sessionUserMap.remove("rest-" + userId);
    }

    public @NonNull Set<Long> getOnlineUserIds() {
        return new HashSet<>(sessionUserMap.values());
    }
}
