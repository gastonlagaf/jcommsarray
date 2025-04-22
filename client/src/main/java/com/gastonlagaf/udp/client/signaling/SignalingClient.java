package com.gastonlagaf.udp.client.signaling;

import com.gastonlagaf.signaling.model.AddressCandidate;
import com.gastonlagaf.signaling.model.Session;
import com.gastonlagaf.signaling.model.SessionEvent;
import com.gastonlagaf.signaling.model.SignalingSubscriber;

import java.io.Closeable;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface SignalingClient extends Closeable {

    CompletableFuture<Session> createSession();

    CompletableFuture<SignalingSubscriber> invite(String sessionId, String subscriberId, List<AddressCandidate> addressCandidates);

    CompletableFuture<Void> removeSubscriber(String sessionId, String subscriberId);

    CompletableFuture<Void> closeSession(String sessionId);

}
