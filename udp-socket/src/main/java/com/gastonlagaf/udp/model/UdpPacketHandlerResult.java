package com.gastonlagaf.udp.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.net.InetSocketAddress;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class UdpPacketHandlerResult {

    private final InetSocketAddress senderAddress;

    private final List<InetSocketAddress> targetAddresses;

    private final byte[] data;

    private final Boolean closeChannel;

    public UdpPacketHandlerResult(InetSocketAddress senderAddress, InetSocketAddress targetAddress, byte[] data) {
        this(senderAddress, List.of(targetAddress), data, false);
    }

    public UdpPacketHandlerResult(InetSocketAddress senderAddress, InetSocketAddress targetAddress, byte[] data, Boolean closeChannel) {
        this(senderAddress, List.of(targetAddress), data, closeChannel);
    }

}
