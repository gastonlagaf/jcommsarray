package com.jcommsarray.test.signaling;

import com.jcommsarray.client.bootstrap.ExchangeSession;
import com.jcommsarray.client.bootstrap.PeerConnection;
import com.jcommsarray.client.ice.model.Candidate;
import com.jcommsarray.client.ice.model.CandidateType;
import com.jcommsarray.client.ice.model.IceRole;
import com.jcommsarray.client.protocol.PureProtocol;
import com.jcommsarray.signaling.SignalingEventHandler;
import com.jcommsarray.signaling.model.AddressCandidate;
import com.jcommsarray.signaling.model.ClosingEvent;
import com.jcommsarray.signaling.model.InviteEvent;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Slf4j
public class SampleSignalingEventHandler implements SignalingEventHandler {

    private final ExchangeSession<PureProtocol> exchangeSession;

    public SampleSignalingEventHandler(ExchangeSession<PureProtocol> exchangeSession) {
        this.exchangeSession = exchangeSession;
    }

    @Override
    public List<AddressCandidate> handleInvite(InviteEvent event) {
        SortedSet<Candidate> opponentCandidates = event.getAddresses().stream()
                .map(it -> new Candidate(it.getValue(), CandidateType.valueOf(it.getType()), it.getPriority()))
                .collect(Collectors.toCollection(TreeSet::new));

        PeerConnection<PureProtocol> peerConnection = exchangeSession.register(event.getUserId())
                .as(IceRole.CONTROLLED)
                .connectTo(event.getUserId())
                .mapEstablishedConnection(it -> {
                    log.info("Established connection: {}", it);
                    return new PureProtocol(it, true);
                })
                .build();

        peerConnection.connect(opponentCandidates);

        return peerConnection.getAddressCandidates();
    }

    @SneakyThrows
    @Override
    public void handleClose(ClosingEvent event) {
        log.info("WS event closed: {}", event.getSessionId());
        exchangeSession.getPeerConnection(event.getUserId()).close();
    }

}
