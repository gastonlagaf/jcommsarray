package com.jcommsarray.client.ice.impl;

import com.jcommsarray.client.ice.IceConnector;
import com.jcommsarray.client.ice.auth.IceUserProvider;
import com.jcommsarray.client.ice.candidate.CandidateSpotter;
import com.jcommsarray.client.ice.check.Checklist;
import com.jcommsarray.client.ice.model.Candidate;
import com.jcommsarray.client.ice.model.CandidatePair;
import com.jcommsarray.client.ice.model.IceProperties;
import com.jcommsarray.client.ice.model.IceSession;
import com.jcommsarray.client.ice.protocol.IceProtocol;
import com.jcommsarray.client.ice.transfer.CandidateTransferOperator;
import com.jcommsarray.client.ice.transfer.model.PeerConnectDetails;
import com.jcommsarray.client.model.ClientProperties;
import com.jcommsarray.client.model.ConnectResult;
import com.jcommsarray.test.socket.UdpSockets;
import com.jcommsarray.turn.integrity.integrity.IntegrityVerifier;
import com.jcommsarray.turn.integrity.integrity.impl.DefaultIntegrityVerifier;
import com.jcommsarray.turn.integrity.user.model.UserDetails;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class DefaultIceConnector implements IceConnector {

    private final IceSession iceSession;

    private final IceProperties iceProperties;

    private final CandidateTransferOperator candidateTransferOperator;

    private final UserDetails userDetails;

    @Getter
    private final SortedSet<Candidate> localCandidates;

    private final CompletableFuture<ConnectResult<IceProtocol>> future = new CompletableFuture<>();

    private final IceUserProvider iceUserProvider = new IceUserProvider();

    public DefaultIceConnector(UdpSockets sockets, IceProperties iceProperties, ClientProperties clientProperties, CandidateTransferOperator candidateTransferOperator) {
        this(sockets, iceProperties, clientProperties, candidateTransferOperator, null);
    }

    public DefaultIceConnector(UdpSockets sockets, IceProperties iceProperties, ClientProperties clientProperties, CandidateTransferOperator candidateTransferOperator, String password) {
        this.iceProperties = iceProperties;
        this.candidateTransferOperator = candidateTransferOperator;
        this.iceSession = new IceSession(iceProperties.getRole(), new Random().nextLong());

        String userPassword = Optional.ofNullable(password).orElseGet(() -> UUID.randomUUID().toString());
        this.userDetails = new UserDetails(iceProperties.getSourceContactId(), userPassword, iceProperties.getRealm());

        IntegrityVerifier integrityVerifier = new DefaultIntegrityVerifier(iceProperties.getRealm(), iceUserProvider);
        CandidateSpotter candidateSpotter = new CandidateSpotter(sockets, iceSession, iceProperties, clientProperties, future, userDetails, integrityVerifier);
        this.localCandidates = candidateSpotter.search();
    }

    @Override
    public CompletableFuture<ConnectResult<IceProtocol>> connect(String opponentId) {
        log.info("Initiating connection with {} as {}", opponentId, iceSession.getRole());
        PeerConnectDetails peerConnectDetails = candidateTransferOperator.exchange(
                iceProperties.getSourceContactId(), iceProperties.getTargetContactId(), localCandidates, userDetails.getPassword()
        );
        return connect(opponentId, peerConnectDetails);
    }

    @Override
    public CompletableFuture<ConnectResult<IceProtocol>> connect(String opponentId, PeerConnectDetails peerConnectDetails) {
        UserDetails opponentDetails = new UserDetails(opponentId, peerConnectDetails.getPassword(), userDetails.getRealm());
        iceUserProvider.setTargetUser(opponentDetails);

        log.info("Connecting to {} as {}", opponentId, iceSession.getRole());
        SortedSet<CandidatePair> candidatePairs = formPairs(localCandidates, peerConnectDetails.getCandidates());

        Checklist checklist = new Checklist(iceProperties.getRetries(), iceSession, candidatePairs, future, iceProperties.getSourceContactId());

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
        for (Candidate localCandidate : localCandidates) {
            for (Candidate remoteCandidate : remoteCandidates) {
                CandidatePair pair = new CandidatePair(iceProperties.getRole(), localCandidate, remoteCandidate);
                result.add(pair);
            }
        }
        return result;
    }

}
