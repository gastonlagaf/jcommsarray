package com.gastonlagaf.udp.test;

import com.gastonlagaf.signaling.model.SignalingSubscriber;
import com.gastonlagaf.udp.client.bootstrap.ExchangeSession;
import com.gastonlagaf.udp.client.bootstrap.ExchangeSessionBuilder;
import com.gastonlagaf.udp.client.bootstrap.PeerConnection;
import com.gastonlagaf.udp.client.ice.model.IceRole;
import com.gastonlagaf.udp.client.ice.transfer.CandidateTransferOperator;
import com.gastonlagaf.udp.client.ice.transfer.impl.DefaultCandidateTransferOperator;
import com.gastonlagaf.udp.client.model.ConnectResult;
import com.gastonlagaf.udp.client.model.SignalingProperties;
import com.gastonlagaf.udp.client.protocol.PureProtocol;
import com.gastonlagaf.udp.client.signaling.SignalingClient;
import com.gastonlagaf.udp.client.signaling.SignalingEventHandler;
import com.gastonlagaf.udp.client.signaling.impl.DefaultSignalingClient;
import com.gastonlagaf.udp.socket.UdpSockets;
import com.gastonlagaf.udp.test.signaling.SampleSignalingEventHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Slf4j
public class IceInitiator {

    public static void main(String[] args) throws IOException {
        UdpSockets sockets = new UdpSockets(1);
        sockets.start();

        ExchangeSession<PureProtocol> exchangeSession = new ExchangeSessionBuilder<PureProtocol>(sockets)
                .withHostId("boba")
                .useStun(new InetSocketAddress("45.129.186.80", 3478))
                .useTurn(new InetSocketAddress("45.129.186.80", 3478))
                .useSocketTimeout(Duration.ofMillis(500L))
                .build();

        CandidateTransferOperator candidateTransferOperator = getCandidateTransferOperator(exchangeSession);

        PeerConnection<PureProtocol> peerConnection = exchangeSession.register("pupa")
                .as(IceRole.CONTROLLING)
                .connectTo("pupa")
                .useCandidateTransferOperator(candidateTransferOperator)
                .mapEstablishedConnection(it -> new PureProtocol(it, false))
                .build();

        peerConnection.connect().thenAccept(IceInitiator::test).join();

        candidateTransferOperator.close();
        exchangeSession.close();
        sockets.close();
    }

    private static CandidateTransferOperator getCandidateTransferOperator(ExchangeSession<PureProtocol> exchangeSession) {
        SignalingProperties signalingProperties = new SignalingProperties(
                URI.create("ws://45.129.186.80:8080/ws"),
                Duration.ofSeconds(20L)
        );
        SignalingEventHandler eventHandler = new SampleSignalingEventHandler(exchangeSession);
        SignalingClient signalingClient = new DefaultSignalingClient(
                signalingProperties, new SignalingSubscriber("boba", List.of()), eventHandler
        );
        return new DefaultCandidateTransferOperator<>(signalingClient, exchangeSession);
    }

    private static void test(ConnectResult<PureProtocol> connectResult) {
        PureProtocol protocol = connectResult.getProtocol();
        Instant start = Instant.now();
        for (int i = 0; i < 60; i++) {
            protocol.getClient().sendAndReceive(connectResult.getOpponentAddress(), "Ping " + i).join();
        }
        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);
        log.info("Sent 60 frames in {} ms", duration.toMillis());
        try {
            protocol.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
