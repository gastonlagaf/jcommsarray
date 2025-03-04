package com.gastonlagaf.udp.turn.server.handler.impl;

import com.gastonlagaf.udp.turn.exception.StunProtocolException;
import com.gastonlagaf.udp.turn.model.*;
import com.gastonlagaf.udp.turn.server.handler.StunMessageHandler;
import com.gastonlagaf.udp.turn.server.model.ContexedMessage;
import com.gastonlagaf.udp.turn.server.model.StunResponse;
import com.gastonlagaf.udp.turn.server.turn.TurnSession;
import lombok.RequiredArgsConstructor;

import java.net.InetSocketAddress;
import java.util.Map;

@RequiredArgsConstructor
public class ChannelBindMessageHandler implements StunMessageHandler {

    @Override
    public MessageType getMessageType() {
        return MessageType.CHANNEL_BIND;
    }

    @Override
    public StunResponse handle(InetSocketAddress serverAddress, InetSocketAddress clientAddress, ContexedMessage message) {
        if (null == message.getTurnSession()) {
            throw new StunProtocolException("Turn session has not been allocated", ErrorCode.ALLOCATION_MISMATCH.getCode());
        }
        AddressAttribute peer = getPeerAttribute(message);

        ChannelNumberAttribute channelNumberAttribute = getChannelNumberAttribute(message, message.getTurnSession());

        if (message.getTurnSession().contains(channelNumberAttribute.getValue(), peer.toInetSocketAddress())) {
            throw new StunProtocolException("Turn session is already allocated", ErrorCode.ALLOCATION_MISMATCH.getCode());
        }
        message.getTurnSession().putChannel(channelNumberAttribute.getValue(), peer.toInetSocketAddress());

        MessageHeader header = new MessageHeader(MessageType.CHANNEL_BIND.getCode(), 0, message.getHeader().getTransactionId());
        Message responseMessage = new Message(header, Map.of());
        return new StunResponse(serverAddress, clientAddress, responseMessage);
    }

    private ChannelNumberAttribute getChannelNumberAttribute(Message message, TurnSession turnSession) {
        ChannelNumberAttribute channelNumberAttribute = message.getAttributes().get(KnownAttributeName.CHANNEL_NUMBER);
        if (null == channelNumberAttribute) {
            throw new StunProtocolException("Channel number not provided", ErrorCode.BAD_REQUEST.getCode());
        }
        Integer channelNumber = channelNumberAttribute.getValue();
        if (ChannelNumberAttribute.MIN_CHANNEL_NUMBER > channelNumber || ChannelNumberAttribute.MAX_CHANNEL_NUMBER < channelNumber) {
            throw new StunProtocolException("Channel number is out of range", ErrorCode.BAD_REQUEST.getCode());
        }
        if (turnSession.containsChannel(channelNumber)) {
            throw new StunProtocolException("Requested channel is already bound", ErrorCode.BAD_REQUEST.getCode());
        }
        return channelNumberAttribute;
    }

    private AddressAttribute getPeerAttribute(Message message) {
        AddressAttribute peer = message.getAttributes().get(KnownAttributeName.XOR_PEER_ADDRESS);
        if (null == peer) {
            throw new StunProtocolException("None peers provided", ErrorCode.BAD_REQUEST.getCode());
        }
        if (!IpFamily.IPV4.equals(peer.getIpFamily())) {
            throw new StunProtocolException("Some peers has different address family", ErrorCode.PEER_ADDRESS_FAMILY_MISMATCH.getCode());
        }
        return peer;
    }

}
