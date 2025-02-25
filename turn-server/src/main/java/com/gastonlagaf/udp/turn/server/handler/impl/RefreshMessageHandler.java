package com.gastonlagaf.udp.client.stun.server.handler.impl;

import com.gastonlagaf.udp.client.stun.exception.StunProtocolException;
import com.gastonlagaf.udp.client.stun.model.*;
import com.gastonlagaf.udp.client.stun.server.handler.StunMessageHandler;
import com.gastonlagaf.udp.client.stun.server.model.ContexedMessage;
import com.gastonlagaf.udp.client.stun.server.model.StunResponse;
import com.gastonlagaf.udp.client.stun.server.turn.TurnSessions;
import lombok.RequiredArgsConstructor;

import java.net.InetSocketAddress;
import java.util.Map;

@RequiredArgsConstructor
public class RefreshMessageHandler implements StunMessageHandler {

    private final Map<String, TurnSessions> turnSessionsMap;

    @Override
    public MessageType getMessageType() {
        return MessageType.REFRESH;
    }

    @Override
    public StunResponse handle(InetSocketAddress serverAddress, InetSocketAddress clientAddress, ContexedMessage message) {
        if (null == message.getTurnSession()) {
            throw new StunProtocolException("Turn session has not been allocated", ErrorCode.ALLOCATION_MISMATCH.getCode());
        }
        TurnSessions turnSessions = turnSessionsMap.get(serverAddress.getAddress().getHostAddress());

        LifetimeAttribute lifetimeAttribute = message.getAttributes().get(KnownAttributeName.LIFETIME);

        boolean closeChannel = LifetimeAttribute.DELETE_ALLOCATION_LIFETIME_MARK.equals(lifetimeAttribute.getValue());
        Integer lifetime = closeChannel
                ? LifetimeAttribute.DELETE_ALLOCATION_LIFETIME_MARK
                : TurnSessions.DEFAULT_SESSION_DURATION_MINUTES.intValue();

        if (closeChannel) {
            turnSessions.remove(message.getTurnSession());
        } else {
            turnSessions.put(message.getTurnSession(), Protocol.UDP);
        }

        MessageHeader messageHeader = new MessageHeader(MessageType.REFRESH.getCode(), 0, message.getHeader().getTransactionId());
        Map<Integer, MessageAttribute> attributes = prepareAttributes(lifetime);
        Message responseMessage = new Message(messageHeader, attributes);

        return new StunResponse(serverAddress, clientAddress, responseMessage, closeChannel);
    }

    private Map<Integer, MessageAttribute> prepareAttributes(Integer lifetime) {
        return Map.of(
                KnownAttributeName.LIFETIME.getCode(), new LifetimeAttribute(lifetime)
        );
    }

}
