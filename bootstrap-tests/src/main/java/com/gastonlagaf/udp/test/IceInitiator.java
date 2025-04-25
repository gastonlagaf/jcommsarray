package com.gastonlagaf.udp.test;

import com.gastonlagaf.signaling.model.SignalingSubscriber;
import com.gastonlagaf.udp.client.bootstrap.ClientBootstrap;
import com.gastonlagaf.udp.client.ice.IceConnector;
import com.gastonlagaf.udp.client.ice.impl.DefaultIceConnector;
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
import com.gastonlagaf.udp.socket.UdpSockets;
import com.gastonlagaf.udp.client.protocol.PureProtocol;
import com.gastonlagaf.udp.test.signaling.SampleSignalingEventHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class IceInitiator {

    public static void main(String[] args) throws IOException {
        UdpSockets sockets = new UdpSockets(1);
        sockets.start();

        new ClientBootstrap<PureProtocol>(sockets)
                .as(IceRole.CONTROLLING)
                .withHostId("boba")
                .connectTo("pupa")
                .useSignaling(URI.create("ws://45.129.186.80:8080/ws"))
                .useStun(new InetSocketAddress("45.129.186.80", 3478))
                .useTurn(new InetSocketAddress("45.129.186.80", 3478))
                .mapEstablishedConnection(it -> new PureProtocol(it, false))
                .connect()
                .thenAccept(it -> {
                    PureProtocol protocol = it.getProtocol();
                    Instant start = Instant.now();
                    for (int i = 0; i < 60; i++) {
                        protocol.getClient().send(it.getOpponentAddress(), "Ping " + i).join();
                    }
                    Instant end = Instant.now();
                    Duration duration = Duration.between(start, end);
                    log.info("Sent 60 frames in {} ms", duration.toMillis());
                    try {
                        protocol.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).join();
        sockets.close();
    }

}
