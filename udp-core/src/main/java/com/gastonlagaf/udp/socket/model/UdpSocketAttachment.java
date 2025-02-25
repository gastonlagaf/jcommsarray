package com.gastonlagaf.udp.socket.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.net.InetSocketAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.BiConsumer;

@RequiredArgsConstructor
@Getter
public class UdpSocketAttachment {

    private final String id;

    private final InetSocketAddress socketAddress;

    private final BiConsumer<String, InetSocketAddress> closeListener;

    private final BlockingQueue<UdpWriteEntry> writingQueue = new LinkedBlockingQueue<>(128);

    @Setter
    private Boolean scheduleClose = false;

}
