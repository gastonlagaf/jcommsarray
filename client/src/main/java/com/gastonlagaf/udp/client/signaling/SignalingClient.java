package com.gastonlagaf.udp.client.signaling;

import com.gastonlagaf.signaling.model.SessionEvent;
import com.gastonlagaf.signaling.model.SignalingEvent;

import java.io.Closeable;
import java.util.concurrent.CompletableFuture;

public interface SignalingClient extends Closeable {

    void send(SignalingEvent event);

    <T extends SessionEvent> CompletableFuture<T> sendAndReceive(SessionEvent event, Class<T> expectedType);

}
