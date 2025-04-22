package com.gastonlagaf.udp.test;

import com.gastonlagaf.signaling.model.AddressCandidate;
import com.gastonlagaf.signaling.model.Session;
import com.gastonlagaf.signaling.model.SignalingSubscriber;
import com.gastonlagaf.udp.client.model.SignalingProperties;
import com.gastonlagaf.udp.client.signaling.SignalingClient;
import com.gastonlagaf.udp.client.signaling.impl.DefaultSignalingClient;
import com.gastonlagaf.udp.client.signaling.impl.NoOpSignalingEventHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.time.Duration;
import java.util.List;

public class SignalingSenderClient {

    public static void main(String[] args) throws IOException {
        SignalingProperties properties = new SignalingProperties(
                URI.create("ws://127.0.0.1:8080/ws"), Duration.ofSeconds(20L)
        );
        SignalingSubscriber signalingSubscriber = new SignalingSubscriber("op1", List.of(
                new AddressCandidate(1, "HOST", InetSocketAddress.createUnresolved("127.0.0.1", 5123)),
                new AddressCandidate(1, "HOST", InetSocketAddress.createUnresolved("192.168.0.101", 5123))
        ));
        SignalingClient client = new DefaultSignalingClient(properties, signalingSubscriber, new NoOpSignalingEventHandler());
        Session session = client.createSession().join();
        SignalingSubscriber subscriber = client.invite(session.getId(), "op2", signalingSubscriber.getAddresses()).join();
        System.out.println(subscriber.getId());
        client.close();
//        client.removeSubscriber(session.getId(), "op2").join();
//        client.closeSession(session.getId()).join();
    }

}
