package com.gastonlagaf.udp.test;

import com.gastonlagaf.signaling.model.SignalingSubscriber;
import com.gastonlagaf.udp.client.model.ClientProperties;
import com.gastonlagaf.udp.client.model.SignalingProperties;
import com.gastonlagaf.udp.client.signaling.SignalingClient;
import com.gastonlagaf.udp.client.signaling.SignalingEventHandler;
import com.gastonlagaf.udp.client.signaling.impl.DefaultSignalingClient;
import com.gastonlagaf.udp.discovery.InternetDiscovery;
import com.gastonlagaf.udp.socket.UdpSockets;
import com.gastonlagaf.udp.test.signaling.SampleSignalingEventHandler;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.time.Duration;
import java.util.List;

public class IceReceiver {

    public static void main(String[] args) throws IOException {
        UdpSockets sockets = new UdpSockets(1);
        sockets.start();

        SignalingProperties signalingProperties = new SignalingProperties(URI.create("ws://45.129.186.80:8080/ws"), Duration.ofSeconds(20L));
        SignalingEventHandler eventHandler = new SampleSignalingEventHandler(sockets);
        SignalingClient signalingClient = new DefaultSignalingClient(signalingProperties, new SignalingSubscriber("pupa", List.of()), eventHandler);

        System.out.println("Initiated signaling client. Awaiting for connection requests...");

        System.in.read();
    }

}
