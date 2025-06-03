package com.jcommsarray.test;

import com.jcommsarray.client.bootstrap.ExchangeSession;
import com.jcommsarray.client.bootstrap.ExchangeSessionBuilder;
import com.jcommsarray.client.bootstrap.PeerConnection;
import com.jcommsarray.client.ice.model.IceRole;
import com.jcommsarray.client.ice.transfer.CandidateTransferOperator;
import com.jcommsarray.client.ice.transfer.impl.DefaultCandidateTransferOperator;
import com.jcommsarray.client.model.ConnectResult;
import com.jcommsarray.client.model.SignalingProperties;
import com.jcommsarray.client.protocol.PureProtocol;
import com.jcommsarray.test.signaling.SampleSignalingEventHandler;
import com.jcommsarray.client.signaling.SignalingClient;
import com.jcommsarray.client.signaling.SignalingEventHandler;
import com.jcommsarray.client.signaling.impl.DefaultSignalingClient;
import com.jcommsarray.signaling.model.SignalingSubscriber;
import com.jcommsarray.test.socket.UdpSockets;
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
