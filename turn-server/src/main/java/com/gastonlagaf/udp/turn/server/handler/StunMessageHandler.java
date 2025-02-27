package com.gastonlagaf.udp.turn.server.handler;

import com.gastonlagaf.udp.turn.model.MessageType;
import com.gastonlagaf.udp.turn.server.model.ContexedMessage;
import com.gastonlagaf.udp.turn.server.model.StunResponse;

import java.net.InetSocketAddress;

public interface StunMessageHandler {

    MessageType getMessageType();

    StunResponse handle(InetSocketAddress serverAddress, InetSocketAddress clientAddress, ContexedMessage message);

}
