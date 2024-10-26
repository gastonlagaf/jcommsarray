package com.gastonlagaf.stun.server.model;

import com.gastonlagaf.stun.model.Message;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.net.InetSocketAddress;

@Getter
@RequiredArgsConstructor
public class StunResponse {

    private final InetSocketAddress sourceAddress;

    private final InetSocketAddress targetAddress;

    private final Message message;

    private final Boolean closeChannel;

    public StunResponse(InetSocketAddress sourceAddress, InetSocketAddress targetAddress, Message message) {
        this(sourceAddress, targetAddress, message, false);
    }
}
