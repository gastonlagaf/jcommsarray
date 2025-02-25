package com.gastonlagaf.udp.client.stun.server.handler.impl;

import com.gastonlagaf.udp.client.stun.model.AddressAttribute;
import com.gastonlagaf.udp.client.stun.model.DefaultMessageAttribute;
import com.gastonlagaf.udp.client.stun.model.KnownAttributeName;
import com.gastonlagaf.udp.client.stun.model.MessageType;
import com.gastonlagaf.udp.client.stun.server.handler.StunMessageHandler;
import com.gastonlagaf.udp.client.stun.server.model.ContexedMessage;
import com.gastonlagaf.udp.client.stun.server.model.StunResponse;
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
