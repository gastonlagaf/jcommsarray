package com.gastonlagaf.udp.client;

import java.io.Closeable;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

public interface UdpClient<T> extends Closeable {

    void send(InetSocketAddress target, T message);

    void send(InetSocketAddress source, InetSocketAddress target, T message);

    CompletableFuture<T> sendAndReceive(InetSocketAddress target, T message);

    CompletableFuture<T> sendAndReceive(InetSocketAddress source, InetSocketAddress target, T message);

}
