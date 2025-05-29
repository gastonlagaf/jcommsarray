package com.jcommsarray.turn.server.handler.impl;

import com.jcommsarray.turn.model.AddressAttribute;
import com.jcommsarray.turn.model.DefaultMessageAttribute;
import com.jcommsarray.turn.model.KnownAttributeName;
import com.jcommsarray.turn.model.MessageType;
import com.jcommsarray.turn.server.handler.StunMessageHandler;
import com.jcommsarray.turn.server.model.ContexedMessage;
import com.jcommsarray.turn.server.model.StunResponse;
import lombok.RequiredArgsConstructor;

import java.net.InetSocketAddress;

@RequiredArgsConstructor
public class SendMessageHandler implements StunMessageHandler {

    @Override
    public MessageType getMessageType() {
        return MessageType.SEND;
    }

    @Override
    public StunResponse handle(InetSocketAddress serverAddress, InetSocketAddress clientAddress, ContexedMessage message) {
        if (null == message.getTurnSession()) {
            return null;
        }
        AddressAttribute peerAddressAttribute = message.getAttributes().get(KnownAttributeName.XOR_PEER_ADDRESS);
        if (null == peerAddressAttribute) {
            return null;
        }
        DefaultMessageAttribute dataAttribute = message.getAttributes().get(KnownAttributeName.DATA);
        if (null == dataAttribute) {
            return null;
        }
        InetSocketAddress targetAddress = peerAddressAttribute.toInetSocketAddress();
        if (!message.getTurnSession().getPeers().contains(targetAddress)) {
            return null;
        }
        return new StunResponse(serverAddress, targetAddress, message);
    }

}
