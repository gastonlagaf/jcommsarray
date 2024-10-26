package com.gastonlagaf.stun.server.handler.impl;

import com.gastonlagaf.stun.model.*;
import com.gastonlagaf.stun.server.handler.StunMessageHandler;
import com.gastonlagaf.stun.server.model.ContexedMessage;
import com.gastonlagaf.stun.server.model.StunResponse;
import lombok.RequiredArgsConstructor;

import java.net.InetSocketAddress;
import java.util.Map;

@RequiredArgsConstructor
public class OutboundChannelDataMessageHandler implements StunMessageHandler {

    @Override
    public MessageType getMessageType() {
        return MessageType.OUTBOUND_CHANNEL_DATA;
    }

    @Override
    public StunResponse handle(InetSocketAddress serverAddress, InetSocketAddress clientAddress, ContexedMessage message) {
        if (null == message.getTurnSession()) {
            return null;
        }

        ChannelNumberAttribute channelNumberAttribute = message.getAttributes().get(KnownAttributeName.CHANNEL_NUMBER);
        InetSocketAddress targetAddress = message.getTurnSession().getAddressByChannel(channelNumberAttribute.getValue());

        MessageHeader header = new MessageHeader(MessageType.SEND);
        Map<Integer, MessageAttribute> attributes = prepareAttributes(message);
        Message forwardedMessage = new Message(header, attributes);
        return new StunResponse(serverAddress, targetAddress, forwardedMessage);
    }

    private Map<Integer, MessageAttribute> prepareAttributes(Message message) {
        return Map.of(
                KnownAttributeName.DATA.getCode(), message.getAttributes().get(KnownAttributeName.DATA)
        );
    }

}
