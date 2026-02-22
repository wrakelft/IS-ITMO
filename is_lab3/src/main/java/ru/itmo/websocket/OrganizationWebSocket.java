package ru.itmo.websocket;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/ws/organizations")
public class OrganizationWebSocket {

    private static final Set<Session> SESSIONS = ConcurrentHashMap.newKeySet();

    @OnOpen
    public void onOpen(Session session) {
        SESSIONS.add(session);
    }

    @OnClose
    public void onClose(Session session) {
        SESSIONS.remove(session);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {

    }

    public static void broadcast(String message) {
        for (Session s : SESSIONS) {
            try {
                s.getBasicRemote().sendText(message);
            } catch (IOException ignored) {}
        }
    }
}
