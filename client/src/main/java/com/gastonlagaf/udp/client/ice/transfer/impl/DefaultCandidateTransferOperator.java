package com.gastonlagaf.udp.client.ice.transfer.impl;

import com.gastonlagaf.signaling.model.AddressCandidate;
import com.gastonlagaf.signaling.model.Session;
import com.gastonlagaf.signaling.model.SignalingSubscriber;
import com.gastonlagaf.udp.client.ice.model.Candidate;
import com.gastonlagaf.udp.client.ice.model.CandidateType;
import com.gastonlagaf.udp.client.ice.transfer.CandidateTransferOperator;
import com.gastonlagaf.udp.client.signaling.SignalingClient;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class DefaultCandidateTransferOperator implements CandidateTransferOperator {

    private final SignalingClient signalingClient;

    private final Session session;

    public DefaultCandidateTransferOperator(SignalingClient signalingClient) {
        this.signalingClient = signalingClient;
        this.session = signalingClient.createSession().join();
    }

    @Override
    public SortedSet<Candidate> exchange(String sourceContactId, String targetContactId, SortedSet<Candidate> candidates) {
        List<AddressCandidate> addressCandidates = candidates.stream()
                .map(it -> new AddressCandidate(it.getPriority(), it.getType().name(), it.getActualAddress()))
                .toList();
        SignalingSubscriber signalingSubscriber = signalingClient.invite(session.getId(), targetContactId, addressCandidates).join();
        return signalingSubscriber.getAddresses().stream()
                .map(it -> new Candidate(it.getValue(), CandidateType.valueOf(it.getType()), it.getPriority()))
                .collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public void close() throws IOException {
        signalingClient.closeSession(session.getId()).join();
        signalingClient.close();
    }
}
