package com.gastonlagaf.stun.server.handler.impl;

import com.gastonlagaf.stun.model.*;
import com.gastonlagaf.stun.server.handler.StunMessageHandler;
import com.gastonlagaf.stun.server.model.ContexedMessage;
import com.gastonlagaf.stun.server.model.StunResponse;
import lombok.RequiredArgsConstructor;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class DataMessageHandler implements StunMessageHandler {

    @Override
    public MessageType getMessageType() {
        return MessageType.DATA;
    }

    @Override
    public StunResponse handle(InetSocketAddress serverAddress, InetSocketAddress clientAddress, ContexedMessage message) {
        if (null == message.getTurnSession()) {
            return null;
        }
        MessageType messageType;
        Integer channelNumber = message.getTurnSession().getChannelByAddress(clientAddress);
        if (null != channelNumber) {
            messageType = MessageType.INBOUND_CHANNEL_DATA;
        } else if (message.getTurnSession().getPeers().contains(clientAddress)) {
            messageType = MessageType.DATA;
        } else {
            return null;
        }

        MessageHeader header = new MessageHeader(messageType);
        Map<Integer, MessageAttribute> attributes = prepareAttributes(clientAddress, message, channelNumber);
        Message forwardedMessage = new Message(header, attributes);
        return new StunResponse(serverAddress, message.getTurnSession().getClientAddress(), forwardedMessage);
    }

    private Map<Integer, MessageAttribute> prepareAttributes(InetSocketAddress senderAddress, Message message, Integer channelNumber) {
        byte[] data = message.getAttributes().<DefaultMessageAttribute>get(KnownAttributeName.DATA).getValue();
        Map<Integer, MessageAttribute> result = new HashMap<>();
        result.put(KnownAttributeName.XOR_PEER_ADDRESS.getCode(), new AddressAttribute(KnownAttributeName.XOR_PEER_ADDRESS, senderAddress));
        result.put(KnownAttributeName.DATA.getCode(), new DefaultMessageAttribute(KnownAttributeName.DATA.getCode(), 0, data));
        if (null != channelNumber) {
            result.put(KnownAttributeName.CHANNEL_NUMBER.getCode(), new ChannelNumberAttribute(KnownAttributeName.CHANNEL_NUMBER.getCode(), Short.BYTES, channelNumber));
        }
        return result;
    }

}
