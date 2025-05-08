package com.gastonlagaf.udp.test.signaling;

import com.gastonlagaf.signaling.model.AddressCandidate;
import com.gastonlagaf.signaling.model.ClosingEvent;
import com.gastonlagaf.signaling.model.InviteEvent;
import com.gastonlagaf.udp.client.bootstrap.ClientBootstrap;
import com.gastonlagaf.udp.client.bootstrap.ClientSession;
import com.gastonlagaf.udp.client.ice.model.Candidate;
import com.gastonlagaf.udp.client.ice.model.CandidateType;
import com.gastonlagaf.udp.client.ice.model.IceRole;
import com.gastonlagaf.udp.client.protocol.PureProtocol;
import com.gastonlagaf.udp.client.signaling.SignalingEventHandler;
import com.gastonlagaf.udp.socket.UdpSockets;
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
