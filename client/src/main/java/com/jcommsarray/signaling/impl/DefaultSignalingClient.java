package com.jcommsarray.signaling.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jcommsarray.client.PendingMessages;
import com.jcommsarray.client.model.SignalingProperties;
import com.jcommsarray.signaling.SignalingClient;
import com.jcommsarray.signaling.SignalingEventHandler;
import com.jcommsarray.signaling.model.*;
import com.jcommsarray.signaling.stomp.JsonStompPayloadCodec;
import com.jcommsarray.signaling.stomp.StompCodec;
import com.jcommsarray.signaling.stomp.model.StompHeaders;
import com.jcommsarray.signaling.stomp.model.StompMessage;
import com.jcommsarray.signaling.stomp.model.StompMessageType;
import lombok.SneakyThrows;

import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class DefaultSignalingClient implements SignalingClient {

    private static final String STOMP_VERSION = "1.2";

    private static final String SUBSCRIBE_DESTINATION = "/user/events";

    private static final Map<Class<? extends SignalingEvent>, String> PATHS = Map.ofEntries(
            Map.entry(RegisterEvent.class, "/actions/subscriptions/register"),
            Map.entry(DeregisterEvent.class, "/actions/subscriptions/deregister"),
            Map.entry(CreateSessionEvent.class, "/actions/sessions/create"),
            Map.entry(SessionClosedEvent.class, "/actions/sessions/%s/destroy"),
            Map.entry(InviteEvent.class, "/actions/sessions/%s/invite"),
            Map.entry(ClosingEvent.class, "/actions/sessions/%s/remove"),
            Map.entry(InviteAnsweredEvent.class, "/actions/sessions/%s/answer"),
            Map.entry(CancelEvent.class, "/actions/sessions/%s/reject"),
            Map.entry(AcknowledgedEvent.class, "/actions/sessions/%s/acknowledge"),
            Map.entry(ClosedEvent.class, "/actions/sessions/%s/leave")
    );

    private final HttpClient httpClient;

    private final WebSocket webSocket;

    private final PendingMessages<SignalingEvent> pendingMessages;

    private final StompCodec stompCodec;

    private final SignalingProperties properties;

    private final SignalingSubscriber signalingSubscriber;

    private final SignalingEventHandler signalingEventHandler;

    public DefaultSignalingClient(SignalingProperties properties, SignalingSubscriber signalingSubscriber, SignalingEventHandler signalingEventHandler) {
        this.properties = properties;
        this.signalingSubscriber = signalingSubscriber;
        this.signalingEventHandler = signalingEventHandler;
        this.pendingMessages = new PendingMessages<>(properties.getReceiveTimeout().toMillis());

        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        JsonStompPayloadCodec jsonStompPayloadCodec = new JsonStompPayloadCodec(objectMapper);
        this.stompCodec = new StompCodec(jsonStompPayloadCodec);

        WebSocketListener listener = new WebSocketListener();

        this.httpClient = HttpClient.newHttpClient();
        this.webSocket = httpClient.newWebSocketBuilder()
                .header("name", signalingSubscriber.getId())
                .buildAsync(properties.getUri(), listener)
                .join();

        connectAndSubscribe();
    }

    @Override
    public CompletableFuture<Session> createSession() {
        return sendAndReceive(new CreateSessionEvent(signalingSubscriber.getId()))
                .thenApply(it -> new Session(it.getSessionId(), it.getUserId(), new HashSet<>()));
    }

    @Override
    public CompletableFuture<SignalingSubscriber> invite(String sessionId, String subscriberId, List<AddressCandidate> addressCandidates) {
        InviteEvent inviteEvent = new InviteEvent(sessionId, subscriberId, addressCandidates);
        return sendAndReceive(inviteEvent)
                .thenApply(it -> {
                    if (it instanceof InviteAnsweredEvent inviteAnsweredEvent) {
                        send(new AcknowledgedEvent(sessionId, subscriberId));
                        return new SignalingSubscriber(it.getUserId(), inviteAnsweredEvent.getAddresses());
                    } else {
                        return null;
                    }
                });
    }

    @Override
    public CompletableFuture<Void> removeSubscriber(String sessionId, String subscriberId) {
        return sendAndReceive(new ClosingEvent(sessionId, subscriberId))
                .thenApply(it -> null);
    }

    @Override
    public CompletableFuture<Void> closeSession(String sessionId) {
        send(new SessionClosedEvent(sessionId, signalingSubscriber.getId()));
        return CompletableFuture.completedFuture(null);
    }

    private void send(SignalingEvent event) {
        String destination = resolveDestination(event);
        StompMessage<SignalingEvent> message = new StompMessage<>(
                StompMessageType.SEND, Map.of(StompHeaders.DESTINATION, destination), event
        );
        String serializedEvent = stompCodec.encode(message);
        System.out.println(serializedEvent);
        webSocket.sendText(serializedEvent, true);
    }

    private void send(StompMessageType type, Map<String, String> headers) {
        StompMessage<Void> message = new StompMessage<>(type, headers, null);
        String serializedEvent = stompCodec.encode(message);
        System.out.println(serializedEvent);
        webSocket.sendText(serializedEvent, true);
    }

    private <T extends SessionEvent> CompletableFuture<T> sendAndReceive(SessionEvent event) {
        CompletableFuture<T> result = pendingMessages.put(event.getUserId())
                .thenApply(it -> (T) it);
        send(event);
        return result;
    }

    private String resolveDestination(SignalingEvent signalingEvent) {
        String path = PATHS.get(signalingEvent.getClass());
        if (signalingEvent instanceof SessionEvent sessionEvent) {
            return String.format(path, sessionEvent.getSessionId());
        }
        return path;
    }

    @SneakyThrows
    private void connectAndSubscribe() {
        send(StompMessageType.CONNECT, Map.of(
                StompHeaders.ACCEPT_VERSION, STOMP_VERSION, StompHeaders.HOST, properties.getUri().getHost()
        ));
        send(StompMessageType.SUBSCRIBE, Map.of(
                StompHeaders.ID, UUID.randomUUID().toString(), StompHeaders.DESTINATION, SUBSCRIBE_DESTINATION
        ));

        RegisterEvent registerEvent = new RegisterEvent(signalingSubscriber);
        send(registerEvent);
    }

    @Override
    public void close() {
        this.webSocket.abort();
        this.httpClient.close();
    }

    private class WebSocketListener implements WebSocket.Listener {

        @Override
        public void onOpen(WebSocket webSocket) {
            WebSocket.Listener.super.onOpen(webSocket);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            StompMessage<SignalingEvent> stompMessage = stompCodec.decode(data, SignalingEvent.class);
            SignalingEvent payload = stompMessage.getPayload();

            if (!(payload instanceof SessionEvent sessionEvent)) {
                return WebSocket.Listener.super.onText(webSocket, data, last);
            }
            switch (sessionEvent) {
                case InviteEvent inviteEvent -> {
                    List<AddressCandidate> addresses = signalingEventHandler.handleInvite(inviteEvent);
                    SessionEvent answer = Optional.ofNullable(addresses).map(it -> !it.isEmpty()).orElse(false)
                            ? new InviteAnsweredEvent(inviteEvent.getSessionId(), signalingSubscriber.getId(), addresses)
                            : new CancelEvent(inviteEvent.getSessionId(), signalingSubscriber.getId(), "Rejected");
                    send(answer);
                }
                case ClosingEvent closingEvent -> {
                    signalingEventHandler.handleClose(closingEvent);
                    send(new ClosedEvent(sessionEvent.getSessionId(), signalingSubscriber.getId()));
                }
                case CancelEvent cancelEvent -> pendingMessages.fail(cancelEvent.getUserId(), cancelEvent.getReason());
                default -> pendingMessages.complete(sessionEvent.getUserId(), sessionEvent);
            }
            return WebSocket.Listener.super.onText(webSocket, data, last);
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            WebSocket.Listener.super.onError(webSocket, error);
        }
    }

}
