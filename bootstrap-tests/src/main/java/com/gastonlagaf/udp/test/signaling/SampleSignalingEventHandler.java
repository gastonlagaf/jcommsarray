package com.gastonlagaf.udp.test.signaling;

import com.gastonlagaf.signaling.model.AddressCandidate;
import com.gastonlagaf.signaling.model.ClosingEvent;
import com.gastonlagaf.signaling.model.InviteEvent;
import com.gastonlagaf.udp.client.ice.IceConnector;
import com.gastonlagaf.udp.client.ice.impl.DefaultIceConnector;
import com.gastonlagaf.udp.client.ice.model.Candidate;
import com.gastonlagaf.udp.client.ice.model.CandidateType;
import com.gastonlagaf.udp.client.ice.model.IceProperties;
import com.gastonlagaf.udp.client.ice.model.IceRole;
import com.gastonlagaf.udp.client.model.ClientProperties;
import com.gastonlagaf.udp.client.signaling.SignalingEventHandler;
import com.gastonlagaf.udp.socket.UdpSockets;
import com.gastonlagaf.udp.test.protocol.PureProtocol;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class SampleSignalingEventHandler implements SignalingEventHandler {

    private final UdpSockets sockets;

    private final ClientProperties clientProperties;

    @Override
    public List<AddressCandidate> handleInvite(InviteEvent event) {
        log.info("Incoming connection request from {}", event.getUserId());
        IceProperties iceProperties = new IceProperties(
                "pupa", "boba", IceRole.CONTROLLED, 1, 5, 40000, 65000
        );
        IceConnector iceConnector = new DefaultIceConnector(sockets, iceProperties, clientProperties, null);
        SortedSet<Candidate> opponentCandidates = event.getAddresses().stream()
                .map(it -> new Candidate(it.getValue(), CandidateType.valueOf(it.getType()), it.getPriority()))
                .collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.<Candidate>reverseOrder())));
        iceConnector.connect(event.getUserId(), opponentCandidates).thenAccept(it -> {
            CompletableFuture.runAsync(() -> {
                PureProtocol pureProtocol = new PureProtocol(it.getIceProtocol(), true);
                try {
                    System.in.read();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        });

        return iceConnector.getLocalCandidates().stream()
                .map(it -> new AddressCandidate(it.getPriority(), it.getType().name(), it.getActualAddress()))
                .toList();
    }

    @Override
    public void handleClose(ClosingEvent event) {
        log.info("WS event closed: {}", event.getSessionId());
    }

}
