package com.gastonlagaf.udp.test;

import com.gastonlagaf.signaling.model.AddressCandidate;
import com.gastonlagaf.signaling.model.ClosingEvent;
import com.gastonlagaf.signaling.model.InviteEvent;
import com.gastonlagaf.signaling.model.SignalingSubscriber;
import com.gastonlagaf.udp.client.ice.IceConnector;
import com.gastonlagaf.udp.client.ice.impl.DefaultIceConnector;
import com.gastonlagaf.udp.client.ice.model.Candidate;
import com.gastonlagaf.udp.client.ice.model.CandidateType;
import com.gastonlagaf.udp.client.ice.model.IceProperties;
import com.gastonlagaf.udp.client.ice.model.IceRole;
import com.gastonlagaf.udp.client.ice.transfer.CandidateTransferOperator;
import com.gastonlagaf.udp.client.ice.transfer.impl.DefaultCandidateTransferOperator;
import com.gastonlagaf.udp.client.model.ClientProperties;
import com.gastonlagaf.udp.client.model.SignalingProperties;
import com.gastonlagaf.udp.client.signaling.SignalingClient;
import com.gastonlagaf.udp.client.signaling.SignalingEventHandler;
import com.gastonlagaf.udp.client.signaling.impl.DefaultSignalingClient;
import com.gastonlagaf.udp.discovery.InternetDiscovery;
import com.gastonlagaf.udp.protocol.Protocol;
import com.gastonlagaf.udp.socket.UdpSockets;
import com.gastonlagaf.udp.test.protocol.PureProtocol;
import com.gastonlagaf.udp.test.signaling.SampleSignalingEventHandler;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class IceInitiator {

    public static void main(String[] args) throws IOException {
        UdpSockets sockets = new UdpSockets(1);
        sockets.start();

        InetAddress hostIp = InternetDiscovery.getAddress();
        IceProperties iceProperties = new IceProperties(
                "boba", "pupa", IceRole.CONTROLLING, 1, 5, 40000, 65000
        );
        ClientProperties clientProperties = new ClientProperties(
                new InetSocketAddress(hostIp, 5129),
                new InetSocketAddress(hostIp, 5128),
                new InetSocketAddress("45.129.186.80", 3478),
                new InetSocketAddress("45.129.186.80", 3478),
//                null,
                5000L
        );

        SignalingProperties signalingProperties = new SignalingProperties(URI.create("ws://45.129.186.80:8080/ws"), Duration.ofSeconds(20L));
        SignalingEventHandler eventHandler = new SampleSignalingEventHandler(sockets, clientProperties);
        SignalingClient signalingClient = new DefaultSignalingClient(signalingProperties, new SignalingSubscriber("boba", List.of()), eventHandler);

        CandidateTransferOperator candidateTransferOperator = new DefaultCandidateTransferOperator(signalingClient);

        IceConnector iceConnector = new DefaultIceConnector(sockets, iceProperties, clientProperties, candidateTransferOperator);
        iceConnector.connect("pupa").thenAccept(it -> {
            CompletableFuture.runAsync(() -> {
                PureProtocol pureProtocol = new PureProtocol(it.getIceProtocol(), false);
                for (int i = 0; i < 100; i++) {
                    pureProtocol.getClient().send(it.getOpponentAddress(), "Ping " + i);
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        });

        System.in.read();
    }

}
