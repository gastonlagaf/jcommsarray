package com.jcommsarray.client.ice.transfer.impl;

import com.jcommsarray.client.bootstrap.ExchangeSession;
import com.jcommsarray.client.ice.model.Candidate;
import com.jcommsarray.client.ice.model.CandidateType;
import com.jcommsarray.client.ice.transfer.CandidateTransferOperator;
import com.jcommsarray.signaling.SignalingClient;
import com.jcommsarray.signaling.model.AddressCandidate;
import com.jcommsarray.signaling.model.SignalingSubscriber;
import com.jcommsarray.test.protocol.ClientProtocol;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class DefaultCandidateTransferOperator<T extends ClientProtocol<?>> implements CandidateTransferOperator {

    private final SignalingClient signalingClient;

    private final ExchangeSession<T> exchangeSession;

    private String sessionId;

    public DefaultCandidateTransferOperator(SignalingClient signalingClient, ExchangeSession<T> exchangeSession) {
        this.signalingClient = signalingClient;
        this.exchangeSession = exchangeSession;
    }

    @Override
    public SortedSet<Candidate> exchange(String sourceContactId, String targetContactId, SortedSet<Candidate> candidates) {
        String sessionId = Optional.ofNullable(this.sessionId).orElseGet(() -> {
            this.sessionId = signalingClient.createSession().join().getId();
            return this.sessionId;
        });
        List<AddressCandidate> addressCandidates = candidates.stream()
                .map(it -> new AddressCandidate(it.getPriority(), it.getType().name(), it.getActualAddress()))
                .toList();
        SignalingSubscriber signalingSubscriber = signalingClient.invite(sessionId, targetContactId, addressCandidates).join();
        return signalingSubscriber.getAddresses().stream()
                .map(it -> new Candidate(it.getValue(), CandidateType.valueOf(it.getType()), it.getPriority()))
                .collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public void close() throws IOException {
        signalingClient.closeSession(sessionId).join();
        signalingClient.close();
    }
}
