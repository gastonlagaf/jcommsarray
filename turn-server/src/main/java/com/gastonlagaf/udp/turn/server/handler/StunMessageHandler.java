package com.gastonlagaf.udp.client.stun.server.handler;

import com.gastonlagaf.udp.client.stun.model.MessageType;
import com.gastonlagaf.udp.client.stun.server.model.ContexedMessage;
import com.gastonlagaf.udp.client.stun.server.model.StunResponse;

import java.net.InetSocketAddress;

public interface StunMessageHandler {

    MessageType getMessageType();

    StunResponse handle(InetSocketAddress serverAddress, InetSocketAddress clientAddress, ContexedMessage message);

}
