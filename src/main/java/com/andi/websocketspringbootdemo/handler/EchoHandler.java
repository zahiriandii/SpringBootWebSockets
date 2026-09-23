package com.andi.websocketspringbootdemo.handler;

import jakarta.websocket.Session;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class EchoHandler extends TextWebSocketHandler
{
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(new ConcurrentWebSocketSessionDecorator(session,5000,64 * 1024));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        sessions.forEach(wsSession -> {
            if (!wsSession.isOpen()) {
                return;
            }
            try {
                wsSession.sendMessage(new TextMessage("Echo: " + payload));
            } catch (IOException e) {
                System.err.println("Send failed to " + wsSession.getId() + ": " + e.getMessage());
            }
        });
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.err.println("Error occurred with session id: " + session.getId() + ":" + exception.getMessage());
        if (session != null && !session.isOpen()) {
            sessions.remove(session);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        System.out.println("Echo closed: " + session.getId() + " " + "reason code:" + status.getCode() + status.getReason());
    }
}
