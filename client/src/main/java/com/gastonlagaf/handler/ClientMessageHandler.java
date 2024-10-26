package com.gastonlagaf.handler;

import com.gastonlagaf.udp.UdpPacketHandler;

import java.io.Serializable;
import java.util.concurrent.CompletableFuture;

public interface ClientMessageHandler<T, K extends Serializable> extends UdpPacketHandler<T> {

    CompletableFuture<T> awaitResult(K key);

}
