package com.gastonlagaf.udp.client;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

public interface UdpClient<T> {

    CompletableFuture<T> registerAwait(T message);

    void send(InetSocketAddress source, InetSocketAddress target, T message);

    T sendAndReceive(InetSocketAddress source, InetSocketAddress target, T message);

}
