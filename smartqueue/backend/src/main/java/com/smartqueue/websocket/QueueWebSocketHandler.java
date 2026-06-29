package com.smartqueue.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartqueue.dto.QueueStatusResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * WebSocket handler for real-time push updates.
 * When any queue state changes (new token, call-next, serve), all connected
 * frontend clients receive the updated queue snapshot instantly.
 *
 * Connect via: ws://localhost:8080/ws/queue
 */
@Component
public class QueueWebSocketHandler extends TextWebSocketHandler {

    // Thread-safe set of active sessions
    private final Set<WebSocketSession> sessions = new CopyOnWriteArraySet<>();
    private final ObjectMapper          mapper   = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        System.out.println("WS connected: " + session.getId() + " | Total: " + sessions.size());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        System.out.println("WS disconnected: " + session.getId() + " | Total: " + sessions.size());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // Clients can send ping; we respond with pong
        if ("ping".equalsIgnoreCase(message.getPayload())) {
            try { session.sendMessage(new TextMessage("pong")); } catch (Exception ignored) {}
        }
    }

    /**
     * Broadcast updated queue state to ALL connected clients.
     * Called by QueueService on every state change.
     */
    public void broadcastQueueUpdate(QueueStatusResponse status) {
        if (sessions.isEmpty()) return;
        try {
            String json = mapper.writeValueAsString(status);
            TextMessage msg = new TextMessage(json);
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    try { session.sendMessage(msg); }
                    catch (Exception e) {
                        sessions.remove(session);  // clean up dead sessions
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("WS broadcast error: " + e.getMessage());
        }
    }

    public int getConnectedCount() { return sessions.size(); }
}
