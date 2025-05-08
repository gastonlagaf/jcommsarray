package com.gastonlagaf.udp;

import com.gastonlagaf.signaling.model.AddressCandidate;
import com.gastonlagaf.signaling.model.ClosingEvent;
import com.gastonlagaf.signaling.model.InviteEvent;
import com.gastonlagaf.signaling.model.SignalingSubscriber;
import com.gastonlagaf.udp.client.bootstrap.ClientBootstrap;
import com.gastonlagaf.udp.client.bootstrap.ClientSession;
import com.gastonlagaf.udp.client.ice.model.Candidate;
import com.gastonlagaf.udp.client.ice.model.CandidateType;
import com.gastonlagaf.udp.client.ice.model.IceRole;
import com.gastonlagaf.udp.client.model.SignalingProperties;
import com.gastonlagaf.udp.client.protocol.PureProtocol;
import com.gastonlagaf.udp.client.signaling.SignalingClient;
import com.gastonlagaf.udp.client.signaling.SignalingEventHandler;
import com.gastonlagaf.udp.client.signaling.impl.DefaultSignalingClient;
import com.gastonlagaf.udp.socket.UdpSockets;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Slf4j
public class ConnectionUtility {

    public static void main(String[] args) {
        UdpSockets udpSockets = new UdpSockets(1);
        udpSockets.start();

        ClientBootstrap<PureProtocol> bootstrap = createClientBootstrap(udpSockets);

        String opponentId = System.getenv("OPPONENT_ID");
        InetSocketAddress targetAddress = Optional.ofNullable(System.getenv("TARGET_ADDRESS"))
                .flatMap(
                        it -> Optional.ofNullable(System.getenv("TARGET_PORT"))
                                .map(Integer::parseInt)
                                .map(ij -> new InetSocketAddress(it, ij))
                )
                .orElse(null);

        if (null == opponentId && null == targetAddress) {
            receiverMode(bootstrap);
        } else {
            initiatorMode(bootstrap, opponentId, targetAddress);
        }

        try {
            udpSockets.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

    }

    private static ClientBootstrap<PureProtocol> createClientBootstrap(UdpSockets udpSockets) {
        ClientBootstrap<PureProtocol> clientBootstrap = new ClientBootstrap<>(udpSockets);

        Optional.ofNullable(System.getenv("SIGNALING_SERVER")).ifPresent(value -> clientBootstrap.useSignaling(URI.create(value)));
        Optional.ofNullable(System.getenv("STUN_SERVER")).ifPresent(value -> clientBootstrap.useStun(new InetSocketAddress(value, 3478)));
        Optional.ofNullable(System.getenv("TURN_SERVER")).ifPresent(value -> clientBootstrap.useTurn(new InetSocketAddress(value, 3478)));
        Optional.ofNullable(System.getenv("HOST_ID")).ifPresent(clientBootstrap::withHostId);

        return clientBootstrap;
    }

    @SneakyThrows
    private static void receiverMode(ClientBootstrap<PureProtocol> clientBootstrap) {
        URI signalingURI = Optional.ofNullable(System.getenv("SIGNALING_SERVER"))
                .map(URI::create)
                .orElseThrow(() -> new IllegalArgumentException("SIGNALING_SERVER is not set"));
        SignalingEventHandler eventHandler = getSignalingEventHandler(clientBootstrap);
        SignalingProperties signalingProperties = new SignalingProperties(signalingURI, Duration.ofSeconds(5L));
        SignalingClient signalingClient = new DefaultSignalingClient(
                signalingProperties,
                new SignalingSubscriber(System.getenv("HOST_ID"), List.of()),
                eventHandler
        );

        System.in.read();
    }

    private static void initiatorMode(ClientBootstrap<PureProtocol> clientBootstrap, String opponentId, InetSocketAddress targetAddress) {
        clientBootstrap.build();
        ClientSession<PureProtocol> clientSession = new ClientSession<>(clientBootstrap);
        Optional.ofNullable(opponentId).ifPresent(clientSession::connectTo);
        Optional.ofNullable(targetAddress).ifPresent(clientSession::connectTo);
        clientSession.as(IceRole.CONTROLLING);
        clientSession.mapEstablishedConnection(it -> new PureProtocol(it, false));

        clientSession.connect().thenAccept(it -> {
            PureProtocol protocol = it.getProtocol();
            Instant start = Instant.now();
            for (int i = 0; i < 60; i++) {
                protocol.getClient().sendAndReceive(it.getOpponentAddress(), "Ping " + i).join();
            }
            Instant end = Instant.now();
            Duration duration = Duration.between(start, end);
            log.info("Sent 60 frames in {} ms", duration.toMillis());
            try {
                protocol.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }).join();
    }

    private static SignalingEventHandler getSignalingEventHandler(ClientBootstrap<PureProtocol> clientBootstrap) {
        return new SignalingEventHandler() {

            @Override
            public List<AddressCandidate> handleInvite(InviteEvent event) {
                SortedSet<Candidate> opponentCandidates = event.getAddresses().stream()
                        .map(it -> new Candidate(it.getValue(), CandidateType.valueOf(it.getType()), it.getPriority()))
                        .collect(Collectors.toCollection(TreeSet::new));

                ClientSession<PureProtocol> clientSession = new ClientSession<>(clientBootstrap)
                        .as(IceRole.CONTROLLED)
                        .connectTo(event.getUserId())
                        .mapEstablishedConnection(it -> {
                            log.info("Established connection: {}", it);
                            return new PureProtocol(it, true);
                        });

                clientSession.connect(opponentCandidates).thenAcceptAsync(it -> {
                    try {
                        System.in.read();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });

                return clientSession.getAddressCandidates();
            }

            @Override
            public void handleClose(ClosingEvent event) {

            }
        };
    }

}
