package com.jcommsarray.client.test;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.List;

import com.jcommsarray.signaling.model.SignalingSubscriber;
import com.jcommsarray.client.model.SignalingProperties;
import com.jcommsarray.signaling.SignalingClient;
import com.jcommsarray.signaling.SignalingEventHandler;
import com.jcommsarray.signaling.impl.DefaultSignalingClient;
import com.jcommsarray.test.socket.UdpSockets;
import com.jcommsarray.client.test.signaling.SampleSignalingEventHandler;

public class IceReceiver {

    public static void main(String[] args) throws IOException {
        UdpSockets sockets = new UdpSockets(1);
        sockets.start();

        SignalingProperties signalingProperties = new SignalingProperties(
                URI.create("ws://45.129.186.80:8080/ws"),
                Duration.ofSeconds(20L)
        );
        SignalingEventHandler eventHandler = new SampleSignalingEventHandler(sockets);
        SignalingClient signalingClient = new DefaultSignalingClient(
                signalingProperties,
                new SignalingSubscriber("pupa", List.of()),
                eventHandler
        );

        System.out.println("Initiated signaling client. Awaiting for connection requests...");

        System.in.read();
    }

}
