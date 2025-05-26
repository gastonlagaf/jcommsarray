package com.jcommsarray.client.bootstrap;

import com.jcommsarray.signaling.model.SignalingSubscriber;
import com.jcommsarray.client.ice.transfer.CandidateTransferOperator;
import com.jcommsarray.client.ice.transfer.impl.DefaultCandidateTransferOperator;
import com.jcommsarray.client.model.SignalingProperties;
import com.jcommsarray.signaling.SignalingClient;
import com.jcommsarray.signaling.impl.DefaultSignalingClient;
import com.jcommsarray.test.protocol.ClientProtocol;
import com.jcommsarray.test.socket.UdpSockets;
import lombok.RequiredArgsConstructor;

import java.net.InetSocketAddress;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class ClientBootstrap<T extends ClientProtocol<?>> {

    final UdpSockets sockets;

    String hostId;

    InetSocketAddress stunAddress;

    InetSocketAddress turnAddress;

    Duration socketTimeout;

    URI signalingUri;

    Duration signalingTimeout;

    CandidateTransferOperator candidateTransferOperator;

    public ClientBootstrap<T> withHostId(String hostId) {
        this.hostId = hostId;
        return this;
    }

    public ClientBootstrap<T> useStun(InetSocketAddress stunAddress) {
        this.stunAddress = stunAddress;
        return this;
    }

    public ClientBootstrap<T> useTurn(InetSocketAddress turnAddress) {
        this.turnAddress = turnAddress;
        return this;
    }

    public ClientBootstrap<T> useSocketTimeout(Duration socketTimeout) {
        this.socketTimeout = socketTimeout;
        return this;
    }

    public ClientBootstrap<T> useSignaling(URI signalingUri) {
        return useSignaling(signalingUri, null);
    }

    public ClientBootstrap<T> useSignaling(URI signalingUri, Duration signalingTimeout) {
        this.signalingUri = signalingUri;
        Optional.ofNullable(signalingTimeout).ifPresent(it -> this.signalingTimeout = signalingTimeout);
        return this;
    }

    public ClientBootstrap<T> useCandidateTransferOperator(CandidateTransferOperator candidateTransferOperator) {
        this.candidateTransferOperator = candidateTransferOperator;
        return this;
    }

    public ClientBootstrap<T> build() {
        this.candidateTransferOperator = initiateDefaultCandidateTransferOperator();
        return this;
    }

    private CandidateTransferOperator initiateDefaultCandidateTransferOperator() {
        SignalingProperties signalingProperties = new SignalingProperties(
                signalingUri, Optional.ofNullable(signalingTimeout).orElseGet(() -> Duration.ofSeconds(10L))
        );
        SignalingClient signalingClient = new DefaultSignalingClient(
                signalingProperties, new SignalingSubscriber(hostId, List.of()), null
        );

        return new DefaultCandidateTransferOperator(signalingClient);
    }

}
