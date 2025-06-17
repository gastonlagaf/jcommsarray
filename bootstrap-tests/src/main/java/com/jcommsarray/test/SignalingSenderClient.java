package com.jcommsarray.test;

import com.jcommsarray.client.model.SignalingProperties;
import com.jcommsarray.client.signaling.SignalingClient;
import com.jcommsarray.client.signaling.impl.DefaultSignalingClient;
import com.jcommsarray.client.signaling.impl.NoOpSignalingEventHandler;
import com.jcommsarray.signaling.model.AddressCandidate;
import com.jcommsarray.signaling.model.Session;
import com.jcommsarray.signaling.model.SignalingSubscriber;

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
        SignalingSubscriber signalingSubscriber = new SignalingSubscriber("op1", null, List.of(
                new AddressCandidate(1, "HOST", InetSocketAddress.createUnresolved("127.0.0.1", 5123)),
                new AddressCandidate(1, "HOST", InetSocketAddress.createUnresolved("192.168.0.101", 5123))
        ));
        SignalingClient client = new DefaultSignalingClient(properties, signalingSubscriber, new NoOpSignalingEventHandler());
        Session session = client.createSession().join();
        SignalingSubscriber subscriber = client.invite(session.getId(), "op2", signalingSubscriber.getAddresses(), "pass1").join();
        System.out.println(subscriber.getId());
        client.close();
//        client.removeSubscriber(session.getId(), "op2").join();
//        client.closeSession(session.getId()).join();
    }

}
