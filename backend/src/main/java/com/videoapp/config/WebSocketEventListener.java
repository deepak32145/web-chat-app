package com.videoapp.config;

import com.videoapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketEventListener {

    @Autowired
    private UserService userService;

    // sessionId -> userId, tracks which user owns each WebSocket session
    private final Map<String, Long> sessionUserMap = new ConcurrentHashMap<>();

    @EventListener
    public void handleConnect(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        List<String> userIdHeaders = accessor.getNativeHeader("userId");
        if (userIdHeaders != null && !userIdHeaders.isEmpty()) {
            try {
                Long userId = Long.parseLong(userIdHeaders.get(0));
                sessionUserMap.put(sessionId, userId);
                userService.setUserOnline(userId);
            } catch (NumberFormatException ignored) {
            }
        }
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        Long userId = sessionUserMap.remove(sessionId);
        if (userId != null) {
            userService.setUserOffline(userId);
        }
    }
}
