package com.gastonlagaf.udp.test;

import com.gastonlagaf.udp.client.bootstrap.ClientBootstrap;
import com.gastonlagaf.udp.client.bootstrap.ClientSession;
import com.gastonlagaf.udp.client.ice.model.IceRole;
import com.gastonlagaf.udp.client.protocol.PureProtocol;
import com.gastonlagaf.udp.socket.UdpSockets;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;

@Slf4j
public class IceInitiator {

    public static void main(String[] args) throws IOException {
        UdpSockets sockets = new UdpSockets(1);
        sockets.start();

        ClientBootstrap<PureProtocol> clientBootstrap = new ClientBootstrap<PureProtocol>(sockets)
                .withHostId("boba")
                .useSignaling(URI.create("ws://45.129.186.80:8080/ws"))
                .useStun(new InetSocketAddress("45.129.186.80", 3478))
                .useTurn(new InetSocketAddress("45.129.186.80", 3478))
                .build();

        new ClientSession<>(clientBootstrap)
                .as(IceRole.CONTROLLING)
                .connectTo("pupa")
                .mapEstablishedConnection(it -> new PureProtocol(it, false))
                .connect()
                .thenAccept(it -> {
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

        sockets.close();
    }

}
