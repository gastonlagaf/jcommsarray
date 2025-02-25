package com.gastonlagaf.udp.client.stun.server.handler.impl;

import com.gastonlagaf.udp.client.stun.exception.StunProtocolException;
import com.gastonlagaf.udp.client.stun.model.*;
import com.gastonlagaf.udp.client.stun.server.handler.StunMessageHandler;
import com.gastonlagaf.udp.client.stun.server.model.ContexedMessage;
import com.gastonlagaf.udp.client.stun.server.model.ServerDispatcher;
import com.gastonlagaf.udp.client.stun.server.model.ServerType;
import com.gastonlagaf.udp.client.stun.server.model.StunResponse;
import lombok.RequiredArgsConstructor;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class BindingRequestMessageHandler implements StunMessageHandler {

    private final ServerDispatcher serverDispatcher;

    @Override
    public MessageType getMessageType() {
        return MessageType.BINDING_REQUEST;
    }

    @Override
    public StunResponse handle(InetSocketAddress serverAddress, InetSocketAddress clientAddress, ContexedMessage message) {
        Map<Integer, MessageAttribute> attributes = prepareAttributes(serverAddress, clientAddress);

        MessageHeader messageHeader = new MessageHeader(
                MessageType.BINDING_REQUEST.getCode(), 0,
                message.getHeader().getTransactionId()
        );

        Message responseMessage = new Message(messageHeader, attributes);

        InetSocketAddress senderAddress = getSenderAddress(serverAddress, message);

        return new StunResponse(senderAddress, clientAddress, responseMessage);
    }

    private Map<Integer, MessageAttribute> prepareAttributes(InetSocketAddress serverAddress, InetSocketAddress clientAddress) {
        Map<Integer, MessageAttribute> attributes = new HashMap<>();
        attributes.put(
                KnownAttributeName.XOR_MAPPED_ADDRESS.getCode(),
                new AddressAttribute(KnownAttributeName.XOR_MAPPED_ADDRESS, clientAddress)
        );
        attributes.put(
                KnownAttributeName.MAPPED_ADDRESS.getCode(),
                new AddressAttribute(KnownAttributeName.MAPPED_ADDRESS, clientAddress)
        );
        attributes.put(
                KnownAttributeName.RESPONSE_ORIGIN.getCode(),
                new AddressAttribute(KnownAttributeName.RESPONSE_ORIGIN, serverAddress)
        );

        ServerType currentServerType = serverDispatcher.getServerType(serverAddress);
        ServerType otherServer = ServerType.getOther(currentServerType);
        InetSocketAddress otherAddress = serverDispatcher.getAddress(otherServer);
        if (null != otherAddress) {
            attributes.put(
                    KnownAttributeName.OTHER_ADDRESS.getCode(),
                    new AddressAttribute(KnownAttributeName.OTHER_ADDRESS, otherAddress)
            );
        }
        return attributes;
    }

    private InetSocketAddress getSenderAddress(InetSocketAddress currentServerAddress, Message message) {
        ServerType currentServerType = serverDispatcher.getServerType(currentServerAddress);
        ChangeRequestAttribute changeRequestAttribute = message.getAttributes()
                .get(KnownAttributeName.CHANGE_REQUEST.getCode());
        if (null == changeRequestAttribute) {
            return serverDispatcher.getAddress(currentServerType);
        }
        ServerType changedServer = ServerType.change(
                currentServerType,
                changeRequestAttribute.getChangeHost(),
                changeRequestAttribute.getChangePort()
        );
        return Optional.ofNullable(serverDispatcher.getAddress(changedServer))
                .orElseThrow(() -> new StunProtocolException(
                        "Server does not support RFC 5780 filtering test",
                        ErrorCode.UNKNOWN_ATTRIBUTE.getCode()
                ));
    }

}
