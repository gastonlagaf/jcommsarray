package com.gastonlagaf.udp.client.bootstrap;

import com.gastonlagaf.udp.protocol.ClientProtocol;
import com.gastonlagaf.udp.socket.UdpSockets;
import lombok.RequiredArgsConstructor;

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class ExchangeSession<T extends ClientProtocol<?>> implements Closeable {

    final UdpSockets sockets;

    final String hostId;

    final InetSocketAddress stunAddress;

    final InetSocketAddress turnAddress;

    final Duration socketTimeout;

    final Integer minPort;

    final Integer maxPort;

    final Map<String, PeerConnection<T>> peerConnections = new ConcurrentHashMap<>();

    public PeerConnectionBuilder<T> register(String key) {
        PeerConnectionBuilder<T> result = new PeerConnectionBuilder<>(this);
        if (null != key) {
            result.onEstablishedConnection(it -> peerConnections.put(key, it));
        }
        return result;
    }

    public Collection<PeerConnection<T>> getPeerConnections() {
        return peerConnections.values();
    }

    public PeerConnection<T> getPeerConnection(String key) {
        return peerConnections.get(key);
    }

    @Override
    public void close() throws IOException {
        peerConnections.values().forEach(it -> {
            try {
                it.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

}
