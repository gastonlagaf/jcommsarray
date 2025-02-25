package com.gastonlagaf.udp.protocol;

import com.gastonlagaf.udp.client.UdpClient;

import java.util.concurrent.CompletableFuture;

public interface ClientProtocol<T> extends Protocol<T> {

    CompletableFuture<T> awaitResult(T message);

    UdpClient<T> getClient();

}
