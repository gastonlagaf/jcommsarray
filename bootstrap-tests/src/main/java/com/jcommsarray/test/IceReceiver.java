package com.jcommsarray.test;

import com.jcommsarray.client.bootstrap.ExchangeSession;
import com.jcommsarray.client.bootstrap.ExchangeSessionBuilder;
import com.jcommsarray.client.ice.transfer.CandidateTransferOperator;
import com.jcommsarray.client.ice.transfer.impl.DefaultCandidateTransferOperator;
import com.jcommsarray.client.model.SignalingProperties;
import com.jcommsarray.client.protocol.PureProtocol;
import com.jcommsarray.test.signaling.SampleSignalingEventHandler;
import com.jcommsarray.client.signaling.SignalingClient;
import com.jcommsarray.client.signaling.SignalingEventHandler;
import com.jcommsarray.client.signaling.impl.DefaultSignalingClient;
import com.jcommsarray.signaling.model.SignalingSubscriber;
import com.jcommsarray.test.socket.UdpSockets;
import lombok.SneakyThrows;

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
