package com.gastonlagaf.udp.protocol.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UdpPacketHandlerResult {

    private final Boolean closeChannel;

    public UdpPacketHandlerResult() {
        this(false);
    }

}
