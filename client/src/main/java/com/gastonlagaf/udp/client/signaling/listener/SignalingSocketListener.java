package com.gastonlagaf.udp.client.signaling.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gastonlagaf.signaling.model.*;
import com.gastonlagaf.udp.client.PendingMessages;
import lombok.RequiredArgsConstructor;

import java.io.UncheckedIOException;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;

@RequiredArgsConstructor
public class SignalingSocketListener implements WebSocket.Listener {

    private final ObjectMapper objectMapper;

    private final PendingMessages<SessionEvent> pendingMessages;

    private final SignalingSubscriber signalingSubscriber;

    @Override
    public void onOpen(WebSocket webSocket) {
        RegisterEvent registerEvent = new RegisterEvent(signalingSubscriber);
        try {
            String serializedEvent = objectMapper.writeValueAsString(registerEvent);
            webSocket.sendText(serializedEvent, true);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
        WebSocket.Listener.super.onOpen(webSocket);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        SignalingEvent event = objectMapper.convertValue(data, SignalingEvent.class);
        if (event instanceof SessionEvent sessionEvent) {
            pendingMessages.complete(sessionEvent.getUserId(), sessionEvent);
        }
        return WebSocket.Listener.super.onText(webSocket, data, last);
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        DeregisterEvent deregisterEvent = new DeregisterEvent();
        try {
            String serializedEvent = objectMapper.writeValueAsString(deregisterEvent);
            webSocket.sendText(serializedEvent, true);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
        return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
    }

}
