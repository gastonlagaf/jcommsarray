package com.gastonlagaf.udp.client.signaling.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gastonlagaf.signaling.model.SessionEvent;
import com.gastonlagaf.signaling.model.SignalingEvent;
import com.gastonlagaf.signaling.model.SignalingSubscriber;
import com.gastonlagaf.udp.client.PendingMessages;
import com.gastonlagaf.udp.client.model.SignalingProperties;
import com.gastonlagaf.udp.client.signaling.SignalingClient;
import com.gastonlagaf.udp.client.signaling.listener.SignalingSocketListener;

import java.io.UncheckedIOException;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletableFuture;

public class DefaultSignalingClient implements SignalingClient {

    private final HttpClient httpClient;

    private final WebSocket webSocket;

    private final PendingMessages<SessionEvent> pendingMessages;

    private final ObjectMapper objectMapper;

    public DefaultSignalingClient(SignalingProperties properties, SignalingSubscriber signalingSubscriber) {
        this.pendingMessages = new PendingMessages<>(properties.getReceiveTimeout().toMillis());
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

        SignalingSocketListener listener = new SignalingSocketListener(objectMapper, pendingMessages, signalingSubscriber);

        this.httpClient = HttpClient.newHttpClient();
        this.webSocket  = httpClient.newWebSocketBuilder()
                .buildAsync(properties.getUri(), listener)
                .join();
    }

    @Override
    public void send(SignalingEvent event) {
        try {
            String serializedEvent = objectMapper.writeValueAsString(event);
            webSocket.sendText(serializedEvent, true);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public <T extends SessionEvent> CompletableFuture<T> sendAndReceive(SessionEvent event, Class<T> expectedType) {
        CompletableFuture<T> result = pendingMessages.put(event.getUserId())
                .thenApply(it -> (T) it);
        send(event);
        return result;
    }

    @Override
    public void close() {
        this.httpClient.close();
    }

}
