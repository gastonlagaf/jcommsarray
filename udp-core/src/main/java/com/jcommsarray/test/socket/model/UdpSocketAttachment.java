package com.jcommsarray.test.socket.model;

import com.jcommsarray.test.protocol.Protocol;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.net.InetSocketAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@RequiredArgsConstructor
@Getter
public class UdpSocketAttachment {

    private final String id;

    private final InetSocketAddress socketAddress;

    private final Protocol<?> protocol;

    private final BlockingQueue<UdpWriteEntry> writingQueue;

    @Setter
    private Boolean scheduleClose = false;

    public UdpSocketAttachment(String id, InetSocketAddress socketAddress, Protocol<?> protocol) {
        this(
                id, socketAddress, protocol, new LinkedBlockingQueue<>(128)
        );
    }

}
