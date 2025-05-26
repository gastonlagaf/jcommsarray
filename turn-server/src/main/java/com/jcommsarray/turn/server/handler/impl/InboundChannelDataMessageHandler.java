package com.jcommsarray.turn.server.handler.impl;

import com.jcommsarray.turn.model.*;
import com.jcommsarray.turn.server.handler.StunMessageHandler;
import com.jcommsarray.turn.server.model.ContexedMessage;
import com.jcommsarray.turn.server.model.StunResponse;
import lombok.RequiredArgsConstructor;

import java.net.InetSocketAddress;
import java.util.Map;

@RequiredArgsConstructor
public class InboundChannelDataMessageHandler implements StunMessageHandler {

    @Override
    public MessageType getMessageType() {
        return MessageType.INBOUND_CHANNEL_DATA;
    }

    @Override
    public StunResponse handle(InetSocketAddress serverAddress, InetSocketAddress clientAddress, ContexedMessage message) {
        Integer channelNumber = message.getTurnSession().getChannelByAddress(clientAddress);
        if (null == channelNumber) {
            return null;
        }

        MessageHeader header = new MessageHeader(MessageType.INBOUND_CHANNEL_DATA);
        Map<Integer, MessageAttribute> attributes = prepareAttributes(message);
        Message forwardedMessage = new Message(header, attributes);

        return new StunResponse(
                message.getTurnSession().getServerAddress(), message.getTurnSession().getClientAddress(), forwardedMessage
        );
    }

    private Map<Integer, MessageAttribute> prepareAttributes(Message message) {
        return Map.of(
                KnownAttributeName.DATA.getCode(), message.getAttributes().get(KnownAttributeName.DATA)
        );
    }

}
