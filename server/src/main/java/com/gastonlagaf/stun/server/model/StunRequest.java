package com.gastonlagaf.stun.server.model;

import com.gastonlagaf.stun.model.Message;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.net.SocketAddress;

@Getter
@RequiredArgsConstructor
public class StunRequest {

    private final SocketAddress socketAddress;

    private final Message message;

}
