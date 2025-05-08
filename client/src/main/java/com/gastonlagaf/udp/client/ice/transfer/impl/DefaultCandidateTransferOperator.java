package com.gastonlagaf.udp.client.ice.transfer.impl;

import com.gastonlagaf.signaling.model.AddressCandidate;
import com.gastonlagaf.signaling.model.Session;
import com.gastonlagaf.signaling.model.SignalingSubscriber;
import com.gastonlagaf.udp.client.ice.model.Candidate;
import com.gastonlagaf.udp.client.ice.model.CandidateType;
import com.gastonlagaf.udp.client.ice.transfer.CandidateTransferOperator;
import com.gastonlagaf.udp.client.signaling.SignalingClient;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class DefaultCandidateTransferOperator implements CandidateTransferOperator {

    private final SignalingClient signalingClient;

    private String sessionId;

    public DefaultCandidateTransferOperator(SignalingClient signalingClient) {
        this.signalingClient = signalingClient;
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
