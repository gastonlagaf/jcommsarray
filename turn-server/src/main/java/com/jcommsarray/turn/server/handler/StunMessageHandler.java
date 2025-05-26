package com.jcommsarray.turn.server.handler;

import com.jcommsarray.turn.model.MessageType;
import com.jcommsarray.turn.server.model.ContexedMessage;
import com.jcommsarray.turn.server.model.StunResponse;

import java.net.InetSocketAddress;

public interface StunMessageHandler {

    MessageType getMessageType();

    StunResponse handle(InetSocketAddress serverAddress, InetSocketAddress clientAddress, ContexedMessage message);

}
