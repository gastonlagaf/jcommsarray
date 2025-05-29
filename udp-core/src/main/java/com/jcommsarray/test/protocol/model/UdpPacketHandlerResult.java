package com.jcommsarray.test.protocol.model;

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
