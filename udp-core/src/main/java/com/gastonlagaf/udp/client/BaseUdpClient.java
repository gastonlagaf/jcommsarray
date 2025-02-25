package com.gastonlagaf.udp.client;

import com.gastonlagaf.udp.protocol.ClientProtocol;
import com.gastonlagaf.udp.socket.UdpSockets;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class BaseUdpClient<T> implements UdpClient<T> {

    protected final UdpSockets<T> udpSockets;

    protected final ClientProtocol<T> clientProtocol;

    protected final InetSocketAddress sourceAddress;

    private final SelectionKey selectionKey;

    public BaseUdpClient(UdpSockets<T> udpSockets) {
        this(udpSockets, null, null);
    }

    public BaseUdpClient(UdpSockets<T> udpSockets, ClientProtocol<T> clientProtocol, InetSocketAddress sourceAddress) {
        this.udpSockets = udpSockets;
        this.clientProtocol = clientProtocol;
        this.sourceAddress = sourceAddress;
        this.selectionKey = Optional.ofNullable(sourceAddress)
                .map(it -> udpSockets.getRegistry().register(it))
                .orElse(null);
    }

    @Override
    public void send(InetSocketAddress target, T message) {
        send(this.sourceAddress, target, message);
    }

    @Override
    public void send(InetSocketAddress source, InetSocketAddress target, T message) {
        Optional.ofNullable(sourceAddress).orElseThrow(() -> new IllegalStateException("Source address not specified"));
        udpSockets.send(source, target, message);
    }

    @Override
    public CompletableFuture<T> sendAndReceive(InetSocketAddress target, T message) {
        return sendAndReceive(this.sourceAddress, target, message);
    }

    @Override
    public CompletableFuture<T> sendAndReceive(InetSocketAddress source, InetSocketAddress target, T message) {
        Optional.ofNullable(sourceAddress).orElseThrow(() -> new IllegalStateException("Source address not specified"));
        Optional.ofNullable(clientProtocol).orElseThrow(() -> new IllegalStateException("Protocol does not exist"));
        CompletableFuture<T> responseFuture = clientProtocol.awaitResult(message);
        udpSockets.send(source, target, message);

        return responseFuture;
    }

    @Override
    public void close() throws IOException {
        Optional.ofNullable(selectionKey).ifPresent(it -> udpSockets.getRegistry().deregister(it));
    }

}
