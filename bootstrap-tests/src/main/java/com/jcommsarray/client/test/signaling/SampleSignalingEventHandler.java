package com.jcommsarray.client.test.signaling;

import com.jcommsarray.signaling.model.AddressCandidate;
import com.jcommsarray.signaling.model.ClosingEvent;
import com.jcommsarray.signaling.model.InviteEvent;
import com.jcommsarray.client.bootstrap.ClientBootstrap;
import com.jcommsarray.client.bootstrap.ClientSession;
import com.jcommsarray.client.ice.model.Candidate;
import com.jcommsarray.client.ice.model.CandidateType;
import com.jcommsarray.client.ice.model.IceRole;
import com.jcommsarray.client.protocol.PureProtocol;
import com.jcommsarray.signaling.SignalingEventHandler;
import com.jcommsarray.test.socket.UdpSockets;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Slf4j
public class SampleSignalingEventHandler implements SignalingEventHandler {

    private final ClientBootstrap<PureProtocol> bootstrap;

    public SampleSignalingEventHandler(UdpSockets sockets) {
        this.bootstrap = new ClientBootstrap<PureProtocol>(sockets)
                .withHostId("pupa")
                .useSignaling(URI.create("ws://45.129.186.80:8080/ws"))
                .useStun(new InetSocketAddress("45.129.186.80", 3478))
                .useTurn(new InetSocketAddress("45.129.186.80", 3478))
                .build();
    }

    @Override
    public List<AddressCandidate> handleInvite(InviteEvent event) {
        SortedSet<Candidate> opponentCandidates = event.getAddresses().stream()
                .map(it -> new Candidate(it.getValue(), CandidateType.valueOf(it.getType()), it.getPriority()))
                .collect(Collectors.toCollection(TreeSet::new));

        ClientSession<PureProtocol> clientSession = new ClientSession<>(bootstrap)
                .as(IceRole.CONTROLLED)
                .connectTo("boba")
                .mapEstablishedConnection(it -> {
                    log.info("Established connection: {}", it);
                    return new PureProtocol(it, true);
                });

        clientSession.connect(opponentCandidates).thenAcceptAsync(it -> {
                try {
                    System.in.read();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
        });

        return clientSession.getAddressCandidates();
    }

    @Override
    public void handleClose(ClosingEvent event) {
        log.info("WS event closed: {}", event.getSessionId());
    }

}
