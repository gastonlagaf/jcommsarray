package com.gastonlagaf.udp.socket.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

@Getter
@RequiredArgsConstructor
public class UdpWriteEntry {

    private final InetSocketAddress target;

    private final ByteBuffer data;

    private final CompletableFuture<Void> future;

}
