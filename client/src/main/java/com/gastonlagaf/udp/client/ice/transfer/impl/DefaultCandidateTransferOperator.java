package com.gastonlagaf.udp.client.ice.transfer.impl;

import com.gastonlagaf.signaling.model.AddressCandidate;
import com.gastonlagaf.signaling.model.Session;
import com.gastonlagaf.signaling.model.SignalingSubscriber;
import com.gastonlagaf.udp.client.bootstrap.ExchangeSession;
import com.gastonlagaf.udp.client.ice.model.Candidate;
import com.gastonlagaf.udp.client.ice.model.CandidateType;
import com.gastonlagaf.udp.client.ice.transfer.CandidateTransferOperator;
import com.gastonlagaf.udp.client.signaling.SignalingClient;
import com.gastonlagaf.udp.protocol.ClientProtocol;

import java.io.IOException;
import java.util.*;
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
