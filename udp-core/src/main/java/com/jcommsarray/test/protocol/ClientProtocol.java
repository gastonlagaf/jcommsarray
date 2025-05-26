package com.jcommsarray.test.protocol;

import com.jcommsarray.client.UdpClient;

import java.util.concurrent.CompletableFuture;

public interface ClientProtocol<T> extends Protocol<T> {

    CompletableFuture<T> awaitResult(T message);

    UdpClient<T> getClient();

}
