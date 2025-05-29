package com.jcommsarray.client;

import java.io.Closeable;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

public interface UdpClient<T> extends Closeable {

    CompletableFuture<Void> send(InetSocketAddress target, T message);

    CompletableFuture<Void> send(InetSocketAddress source, InetSocketAddress target, T message);

    CompletableFuture<T> sendAndReceive(InetSocketAddress target, T message);

    CompletableFuture<T> sendAndReceive(InetSocketAddress source, InetSocketAddress target, T message);

}
