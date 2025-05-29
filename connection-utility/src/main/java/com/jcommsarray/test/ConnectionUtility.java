package com.jcommsarray.test;

import com.jcommsarray.client.bootstrap.*;
import com.jcommsarray.client.ice.model.Candidate;
import com.jcommsarray.client.ice.model.CandidateType;
import com.jcommsarray.client.ice.model.IceRole;
import com.jcommsarray.client.ice.transfer.CandidateTransferOperator;
import com.jcommsarray.client.ice.transfer.impl.DefaultCandidateTransferOperator;
import com.jcommsarray.client.model.ClientProperties;
import com.jcommsarray.client.model.ConnectResult;
import com.jcommsarray.client.model.SignalingProperties;
import com.jcommsarray.client.protocol.PureProtocol;
import com.jcommsarray.signaling.SignalingClient;
import com.jcommsarray.signaling.SignalingEventHandler;
import com.jcommsarray.signaling.impl.DefaultSignalingClient;
import com.jcommsarray.signaling.model.AddressCandidate;
import com.jcommsarray.signaling.model.ClosingEvent;
import com.jcommsarray.signaling.model.InviteEvent;
import com.jcommsarray.signaling.model.SignalingSubscriber;
import com.jcommsarray.test.socket.UdpSockets;
import com.jcommsarray.turn.model.NatBehaviour;
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

    private static final String HOST_ID = "HOST_ID";
    private static final String OPPONENT_ID = "OPPONENT_ID";
    private static final String STUN_SERVER = "STUN_SERVER";
    private static final String TURN_SERVER = "TURN_SERVER";
    private static final String SOCKET_TIMEOUT = "SOCKET_TIMEOUT";
    private static final String TARGET_ADDRESS = "TARGET_ADDRESS";
    private static final String TARGET_PORT = "TARGET_PORT";
    private static final String SIGNALING_SERVER = "SIGNALING_SERVER";
    private static final String PACKETS_QUANTITY = "PACKETS_QUANTITY";
    private static final String PACKETS_SEND_INTERVAL = "PACKETS_SEND_INTERVAL";

    public static void main(String[] args) {
        UdpSockets udpSockets = new UdpSockets(1);
        udpSockets.start();

        String hostId = System.getenv(HOST_ID);
        ExchangeSession<PureProtocol> bootstrap = createExchangeSession(hostId, udpSockets);

        String opponentId = System.getenv(OPPONENT_ID);
        InetSocketAddress targetAddress = getTargetAddress();

        if (null == opponentId && null == targetAddress) {
            receiverMode(hostId, udpSockets, bootstrap);
        } else {
            initiatorMode(bootstrap, hostId, opponentId, targetAddress);
        }

        try {
            udpSockets.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static ExchangeSession<PureProtocol> createExchangeSession(String hostId, UdpSockets udpSockets) {
        ExchangeSessionBuilder<PureProtocol> exchangeSession = new ExchangeSessionBuilder<>(udpSockets);

        Optional.ofNullable(System.getenv(STUN_SERVER))
                .map(value -> new InetSocketAddress(value, 3478))
                .ifPresent(exchangeSession::useStun);
        Optional.ofNullable(System.getenv(TURN_SERVER))
                .map(value -> new InetSocketAddress(value, 3478))
                .ifPresent(exchangeSession::useTurn);
        Optional.ofNullable(System.getenv(SOCKET_TIMEOUT))
                .map(Long::valueOf)
                .map(Duration::ofMillis)
                .ifPresent(exchangeSession::useSocketTimeout);
        Optional.ofNullable(hostId).ifPresent(exchangeSession::withHostId);

        return exchangeSession.build();
    }

    @SneakyThrows
    private static void receiverMode(String hostId, UdpSockets udpSockets, ExchangeSession<PureProtocol> exchangeSession) {
        if (null == hostId) {
            ClientProperties clientProperties = new ClientProperties(
                    null, null, null, null, 500L
            );
            ProtocolInitializer protocolInitializer = new ProtocolInitializer(1024, 65535);
            PureProtocol pureProtocol = protocolInitializer.init(
                    clientProperties,
                    properties -> new PureProtocol(udpSockets, NatBehaviour.NO_NAT, properties, true)
            );
        } else {
            getTransferOperator(hostId, exchangeSession);
        }

        Thread.currentThread().join();
    }

    private static InetSocketAddress getTargetAddress() {
        return Optional.ofNullable(System.getenv(TARGET_ADDRESS))
                .flatMap(
                        it -> Optional.ofNullable(System.getenv(TARGET_PORT))
                                .map(Integer::parseInt)
                                .map(ij -> new InetSocketAddress(it, ij))
                )
                .orElse(null);
    }

    private static void initiatorMode(ExchangeSession<PureProtocol> exchangeSession, String hostId, String opponentId, InetSocketAddress targetAddress) {
        CandidateTransferOperator candidateTransferOperator = null != opponentId ? getTransferOperator(hostId, exchangeSession) : null;

        PeerConnectionBuilder<PureProtocol> peerConnectionBuilder = exchangeSession.register(opponentId)
                .as(IceRole.CONTROLLING)
                .mapEstablishedConnection(it -> new PureProtocol(it, false))
                .useCandidateTransferOperator(candidateTransferOperator);

        Optional.ofNullable(targetAddress).ifPresent(peerConnectionBuilder::connectTo);
        Optional.ofNullable(opponentId).ifPresent(peerConnectionBuilder::connectTo);

        PeerConnection<PureProtocol> peerConnection = peerConnectionBuilder.build();

        peerConnection.connect().thenAccept(it -> test(exchangeSession, it, candidateTransferOperator)).join();
    }

    private static CandidateTransferOperator getTransferOperator(String hostId, ExchangeSession<PureProtocol> exchangeSession) {
        URI signalingURI = Optional.ofNullable(System.getenv(SIGNALING_SERVER))
                .map(URI::create)
                .orElseThrow(() -> new IllegalArgumentException(SIGNALING_SERVER + " is not set"));
        SignalingProperties signalingProperties = new SignalingProperties(signalingURI, Duration.ofSeconds(5));
        SignalingSubscriber signalingSubscriber = new SignalingSubscriber(hostId, List.of());
        SignalingEventHandler eventHandler = getSignalingEventHandler(exchangeSession);
        SignalingClient signalingClient = new DefaultSignalingClient(signalingProperties, signalingSubscriber, eventHandler);

        return new DefaultCandidateTransferOperator<>(signalingClient, exchangeSession);
    }

    private static void test(ExchangeSession<PureProtocol> exchangeSession, ConnectResult<PureProtocol> connectResult, CandidateTransferOperator candidateTransferOperator) {
        int packetsQuantity = Optional.ofNullable(System.getenv(PACKETS_QUANTITY))
                .map(it -> Math.abs(Integer.parseInt(it)))
                .orElse(60);
        long packetsSendInterval = Optional.ofNullable(System.getenv(PACKETS_SEND_INTERVAL))
                .map(it -> Math.abs(Long.parseLong(it)))
                .orElse(0L);
        ;

        PureProtocol protocol = connectResult.getProtocol();

        Instant start = Instant.now();
        for (int i = 0; i < packetsQuantity; i++) {
            protocol.getClient().sendAndReceive(connectResult.getOpponentAddress(), "Ping " + i).join();
            try {
                if (0 == packetsSendInterval) {
                    continue;
                }
                Thread.sleep(packetsSendInterval);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);
        log.info("Sent {} packets in {} ms", packetsQuantity, duration.toMillis());

        try {
            if (null != candidateTransferOperator) {
                candidateTransferOperator.close();
            }
            exchangeSession.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static SignalingEventHandler getSignalingEventHandler(ExchangeSession<PureProtocol> exchangeSession) {

        return new SignalingEventHandler() {

            @Override
            public List<AddressCandidate> handleInvite(InviteEvent event) {
                SortedSet<Candidate> opponentCandidates = event.getAddresses().stream()
                        .map(it -> new Candidate(it.getValue(), CandidateType.valueOf(it.getType()), it.getPriority()))
                        .collect(Collectors.toCollection(TreeSet::new));

                PeerConnection<PureProtocol> peerConnectionBuilder = exchangeSession.register(event.getUserId())
                        .as(IceRole.CONTROLLED)
                        .connectTo(event.getUserId())
                        .mapEstablishedConnection(it -> {
                            log.info("Established connection: {}", it);
                            return new PureProtocol(it, true);
                        })
                        .build();

                peerConnectionBuilder.connect(opponentCandidates);

                return peerConnectionBuilder.getAddressCandidates();
            }

            @Override
            public void handleClose(ClosingEvent event) {

            }
        };

    }

}
