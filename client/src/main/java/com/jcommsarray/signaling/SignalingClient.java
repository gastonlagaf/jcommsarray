package com.jcommsarray.signaling;

import com.jcommsarray.signaling.model.AddressCandidate;
import com.jcommsarray.signaling.model.Session;
import com.jcommsarray.signaling.model.SignalingSubscriber;

import java.io.Closeable;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface SignalingClient extends Closeable {

    CompletableFuture<Session> createSession();

    CompletableFuture<SignalingSubscriber> invite(String sessionId, String subscriberId, List<AddressCandidate> addressCandidates);

    CompletableFuture<Void> removeSubscriber(String sessionId, String subscriberId);

    CompletableFuture<Void> closeSession(String sessionId);

}
