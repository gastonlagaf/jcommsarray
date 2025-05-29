package com.gastonlagaf.udp.test;

import com.gastonlagaf.signaling.model.SignalingSubscriber;
import com.gastonlagaf.udp.client.bootstrap.ExchangeSession;
import com.gastonlagaf.udp.client.bootstrap.ExchangeSessionBuilder;
import com.gastonlagaf.udp.client.ice.transfer.CandidateTransferOperator;
import com.gastonlagaf.udp.client.ice.transfer.impl.DefaultCandidateTransferOperator;
import com.gastonlagaf.udp.client.model.SignalingProperties;
import com.gastonlagaf.udp.client.protocol.PureProtocol;
import com.gastonlagaf.udp.client.signaling.SignalingClient;
import com.gastonlagaf.udp.client.signaling.SignalingEventHandler;
import com.gastonlagaf.udp.client.signaling.impl.DefaultSignalingClient;
import com.gastonlagaf.udp.socket.UdpSockets;
import com.gastonlagaf.udp.test.signaling.SampleSignalingEventHandler;
import lombok.SneakyThrows;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.time.Duration;
import java.util.List;

public class IceReceiver {

    @SneakyThrows
    public static void main(String[] args) {
        UdpSockets sockets = new UdpSockets(1);
        sockets.start();

        ExchangeSession<PureProtocol> exchangeSession = new ExchangeSessionBuilder<PureProtocol>(sockets)
                .withHostId("pupa")
                .useStun(new InetSocketAddress("45.129.186.80", 3478))
                .useTurn(new InetSocketAddress("45.129.186.80", 3478))
                .useSocketTimeout(Duration.ofMillis(500L))
                .build();

        SignalingProperties signalingProperties = new SignalingProperties(
                URI.create("ws://45.129.186.80:8080/ws"),
                Duration.ofSeconds(20L)
        );
        SignalingEventHandler eventHandler = new SampleSignalingEventHandler(exchangeSession);
        SignalingClient signalingClient = new DefaultSignalingClient(
                signalingProperties,
                new SignalingSubscriber("pupa", List.of()),
                eventHandler
        );

        CandidateTransferOperator candidateTransferOperator = new DefaultCandidateTransferOperator<>(
                signalingClient, exchangeSession
        );

        System.out.println("Initiated signaling client. Awaiting for connection requests...");

        Thread.currentThread().join();
    }

}
