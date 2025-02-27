package com.gastonlagaf.udp.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

public abstract class UdpClientDelegate<T> implements UdpClient<T> {

    protected final UdpClient<T> delegate;

    public UdpClientDelegate(UdpClient<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public void send(InetSocketAddress target, T message) {
        delegate.send(target, message);
    }

    @Override
    public void send(InetSocketAddress source, InetSocketAddress target, T message) {
        delegate.send(source, target, message);
    }

    @Override
    public CompletableFuture<T> sendAndReceive(InetSocketAddress target, T message) {
        return delegate.sendAndReceive(target, message);
    }

    @Override
    public CompletableFuture<T> sendAndReceive(InetSocketAddress source, InetSocketAddress target, T message) {
        return delegate.sendAndReceive(source, target, message);
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

}
