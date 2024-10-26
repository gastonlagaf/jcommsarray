package com.gastonlagaf.stun.server.handler.impl;

import com.gastonlagaf.stun.model.AddressAttribute;
import com.gastonlagaf.stun.model.DefaultMessageAttribute;
import com.gastonlagaf.stun.model.KnownAttributeName;
import com.gastonlagaf.stun.model.MessageType;
import com.gastonlagaf.stun.server.handler.StunMessageHandler;
import com.gastonlagaf.stun.server.model.ContexedMessage;
import com.gastonlagaf.stun.server.model.StunResponse;
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
