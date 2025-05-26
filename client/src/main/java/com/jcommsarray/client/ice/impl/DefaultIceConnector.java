package com.jcommsarray.client.ice.impl;

import com.jcommsarray.client.ice.IceConnector;
import com.jcommsarray.client.ice.candidate.CandidateSpotter;
import com.jcommsarray.client.ice.check.Checklist;
import com.jcommsarray.client.ice.model.Candidate;
import com.jcommsarray.client.ice.model.CandidatePair;
import com.jcommsarray.client.ice.model.IceProperties;
import com.jcommsarray.client.ice.model.IceSession;
import com.jcommsarray.client.ice.protocol.IceProtocol;
import com.jcommsarray.client.ice.transfer.CandidateTransferOperator;
import com.jcommsarray.client.model.ClientProperties;
import com.jcommsarray.client.model.ConnectResult;
import com.jcommsarray.test.socket.UdpSockets;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class DefaultIceConnector implements IceConnector {

    private final IceSession iceSession;

    private final IceProperties iceProperties;

    private final CandidateTransferOperator candidateTransferOperator;

    @Getter
    private final SortedSet<Candidate> localCandidates;

    private final CompletableFuture<ConnectResult<IceProtocol>> future = new CompletableFuture<>();

    public DefaultIceConnector(UdpSockets sockets, IceProperties iceProperties, ClientProperties clientProperties, CandidateTransferOperator candidateTransferOperator) {
        this.iceProperties = iceProperties;
        this.candidateTransferOperator = candidateTransferOperator;
        this.iceSession = new IceSession(iceProperties.getRole(), new Random().nextLong());

        CandidateSpotter candidateSpotter = new CandidateSpotter(sockets, iceSession, iceProperties, clientProperties, future);
        this.localCandidates = candidateSpotter.search();
    }

    @Override
    public CompletableFuture<ConnectResult<IceProtocol>> connect(String opponentId) {
        log.info("Initiating connection with {} as {}", opponentId, iceSession.getRole());
        SortedSet<Candidate> targetCandidates = candidateTransferOperator.exchange(
                iceProperties.getSourceContactId(), iceProperties.getTargetContactId(), localCandidates
        );
        return connect(opponentId, targetCandidates);
    }

    @Override
    public CompletableFuture<ConnectResult<IceProtocol>> connect(String opponentId, SortedSet<Candidate> opponentCandidates) {
        log.info("Connecting to {} as {}", opponentId, iceSession.getRole());
        SortedSet<CandidatePair> candidatePairs = formPairs(localCandidates, opponentCandidates);

        Checklist checklist = new Checklist(iceProperties.getRetries(), iceSession, candidatePairs, future);

        return checklist.check().thenApply(it -> {
            closeRedundantSockets(it.getProtocol());
            return it;
        });
    }

    private void closeRedundantSockets(IceProtocol nominatedCandidate) {
        localCandidates.stream()
                .filter(it -> !it.getIceProtocol().equals(nominatedCandidate))
                .forEach(it -> {
                    try {
                        it.getIceProtocol().close();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
    }

    private SortedSet<CandidatePair> formPairs(SortedSet<Candidate> localCandidates, SortedSet<Candidate> remoteCandidates) {
        SortedSet<CandidatePair> result = new TreeSet<>();
        for (Candidate localCandidate: localCandidates) {
            for (Candidate remoteCandidate: remoteCandidates) {
                CandidatePair pair = new CandidatePair(iceProperties.getRole(), localCandidate, remoteCandidate);
                result.add(pair);
            }
        }
        return result;
    }

}
