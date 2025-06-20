package com.jcommsarray.test;

import com.jcommsarray.client.ice.transfer.model.PeerConnectDetails;
import com.jcommsarray.client.model.SignalingProperties;
import com.jcommsarray.client.signaling.SignalingClient;
import com.jcommsarray.client.signaling.SignalingEventHandler;
import com.jcommsarray.client.signaling.impl.DefaultSignalingClient;
import com.jcommsarray.signaling.model.AddressCandidate;
import com.jcommsarray.signaling.model.ClosingEvent;
import com.jcommsarray.signaling.model.InviteEvent;
import com.jcommsarray.signaling.model.SignalingSubscriber;

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
        SignalingSubscriber signalingSubscriber = new SignalingSubscriber("op2", null, List.of(
                new AddressCandidate(1, "HOST", InetSocketAddress.createUnresolved("127.0.0.1", 5126)),
                new AddressCandidate(1, "HOST", InetSocketAddress.createUnresolved("192.168.0.121", 5126))
        ));
        SignalingEventHandler signalingEventHandler = new SignalingEventHandler() {
            @Override
            public PeerConnectDetails handleInvite(InviteEvent event) {
                System.out.println("Received Invite event: " + event.getSessionId());
                return PeerConnectDetails.INSTANCE;
            }

            @Override
            public void handleClose(ClosingEvent event) {

            }
        };
        SignalingClient client = new DefaultSignalingClient(properties, signalingSubscriber, signalingEventHandler);
        System.in.read();
    }

}
