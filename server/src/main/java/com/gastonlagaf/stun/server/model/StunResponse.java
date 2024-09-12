package com.gastonlagaf.stun.server.model;

import com.gastonlagaf.stun.model.Message;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;

@Getter
@RequiredArgsConstructor
public class StunResponse {

    private final DatagramChannel senderChannel;

    private final SocketAddress receiverAddress;

    private final Message message;

}
