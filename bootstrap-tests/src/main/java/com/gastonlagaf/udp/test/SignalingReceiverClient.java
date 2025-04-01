package com.gastonlagaf.udp.test;

import com.gastonlagaf.signaling.model.*;
import com.gastonlagaf.udp.client.model.SignalingProperties;
import com.gastonlagaf.udp.client.signaling.SignalingClient;
import com.gastonlagaf.udp.client.signaling.SignalingEventHandler;
import com.gastonlagaf.udp.client.signaling.impl.DefaultSignalingClient;
import com.gastonlagaf.udp.client.signaling.impl.NoOpSignalingEventHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.time.Duration;
import java.util.List;

public class SignalingReceiverClient {

    public static void main(String[] args) throws IOException {
        SignalingProperties properties = new SignalingProperties(
                URI.create("ws://127.0.0.1:8080/ws"), Duration.ofSeconds(10L)
        );
        SignalingSubscriber signalingSubscriber = new SignalingSubscriber("op2", List.of(
                new AddressCandidate(1, InetSocketAddress.createUnresolved("127.0.0.1", 5126)),
                new AddressCandidate(1, InetSocketAddress.createUnresolved("192.168.0.121", 5126))
        ));
        SignalingEventHandler signalingEventHandler = new SignalingEventHandler() {
            @Override
            public Boolean handleInvite(InviteEvent event) {
                System.out.println("Received Invite event: " + event.getSessionId());
                return true;
            }

            @Override
            public void handleClose(ClosingEvent event) {

            }
        };
        SignalingClient client = new DefaultSignalingClient(properties, signalingSubscriber, signalingEventHandler);
        System.in.read();
    }

}
