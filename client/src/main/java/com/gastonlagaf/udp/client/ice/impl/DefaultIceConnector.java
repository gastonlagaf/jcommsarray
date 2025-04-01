package com.gastonlagaf.udp.client.ice.impl;

import com.gastonlagaf.udp.client.ice.IceConnector;
import com.gastonlagaf.udp.client.ice.candidate.CandidateSpotter;
import com.gastonlagaf.udp.client.ice.model.Candidate;
import com.gastonlagaf.udp.client.ice.model.IceProperties;
import com.gastonlagaf.udp.client.ice.model.IceRole;
import com.gastonlagaf.udp.client.ice.transfer.CandidateTransferOperator;
import com.gastonlagaf.udp.client.model.ClientProperties;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@RequiredArgsConstructor
public class DefaultIceConnector implements IceConnector {

    private final IceProperties iceProperties;

    private final CandidateSpotter candidateSpotter;

    private final CandidateTransferOperator candidateTransferOperator;

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public DefaultIceConnector(IceProperties iceProperties, ClientProperties clientProperties, CandidateTransferOperator candidateTransferOperator) {
        this.iceProperties = iceProperties;
        this.candidateSpotter = new CandidateSpotter(iceProperties, clientProperties);
        this.candidateTransferOperator = candidateTransferOperator;
    }

    @Override
    public Candidate connect(String peerIdentifier) {
        List<Candidate> candidates = candidateSpotter.search();
        List<Candidate> targetCandidates = candidateTransferOperator.exchange(
                iceProperties.getSourceContactId(), iceProperties.getTargetContactId(), candidates
        );
        return null;
    }

}
