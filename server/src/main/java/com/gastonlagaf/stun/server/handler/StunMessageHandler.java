package com.gastonlagaf.stun.server.handler;

import com.gastonlagaf.stun.model.MessageType;
import com.gastonlagaf.stun.server.model.ContexedMessage;
import com.gastonlagaf.stun.server.model.StunResponse;

import java.net.InetSocketAddress;

public interface StunMessageHandler {

    MessageType getMessageType();

    StunResponse handle(InetSocketAddress serverAddress, InetSocketAddress clientAddress, ContexedMessage message);

}
