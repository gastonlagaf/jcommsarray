package com.jcommsarray.client;

import com.jcommsarray.test.protocol.ClientProtocol;
import com.jcommsarray.test.socket.UdpSockets;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class BaseUdpClient<T> implements UdpClient<T> {

    protected final UdpSockets udpSockets;

    protected final ClientProtocol<T> clientProtocol;

    protected final InetSocketAddress sourceAddress;

    public BaseUdpClient(UdpSockets udpSockets, ClientProtocol<T> clientProtocol, InetSocketAddress sourceAddress) {
        this.udpSockets = udpSockets;
        this.clientProtocol = clientProtocol;
        this.sourceAddress = sourceAddress;
    }

    @Override
    public CompletableFuture<Void> send(InetSocketAddress target, T message) {
        return send(this.sourceAddress, target, message);
    }

    @Override
    public CompletableFuture<Void> send(InetSocketAddress source, InetSocketAddress target, T message) {
        Optional.ofNullable(sourceAddress).orElseThrow(() -> new IllegalStateException("Source address not specified"));
        ByteBuffer data = clientProtocol.serialize(message);
        return udpSockets.send(source, target, data);
    }

    @Override
    public CompletableFuture<T> sendAndReceive(InetSocketAddress target, T message) {
        return sendAndReceive(this.sourceAddress, target, message);
    }

    @Override
    public CompletableFuture<T> sendAndReceive(InetSocketAddress source, InetSocketAddress target, T message) {
        Optional.ofNullable(sourceAddress).orElseThrow(() -> new IllegalStateException("Source address not specified"));
        Optional.ofNullable(clientProtocol).orElseThrow(() -> new IllegalStateException("Protocol does not exist"));

        ByteBuffer data = clientProtocol.serialize(message);

        return udpSockets.send(source, target, data).thenCompose(it -> clientProtocol.awaitResult(message));
    }

    @Override
    public void close() throws IOException {
        // No-op
    }

}
