package com.gastonlagaf.stun.server.handler.impl;

import com.gastonlagaf.stun.exception.StunProtocolException;
import com.gastonlagaf.stun.model.*;
import com.gastonlagaf.stun.server.handler.StunMessageHandler;
import com.gastonlagaf.stun.server.model.ServerType;
import com.gastonlagaf.stun.server.model.StunRequest;
import com.gastonlagaf.stun.server.model.StunResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.nio.channels.DatagramChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class DefaultStunMessageHandler implements StunMessageHandler {

    private final Map<ServerType, DatagramChannel> channelMap;

    @Override
    @SneakyThrows
    public StunResponse handle(ServerType serverType, DatagramChannel channel, StunRequest stunRequest) {
        Map<Integer, MessageAttribute> attributes = prepareAttributes(serverType, channel, stunRequest);

        MessageHeader messageHeader = new MessageHeader(
                MessageType.BINDING_SUCCESS_RESPONSE.getCode(), 0,
                stunRequest.getMessage().getHeader().getTransactionId()
        );

        Message message = new Message(messageHeader, attributes);

        try {
            DatagramChannel sendChannel = getSenderChannel(serverType, stunRequest.getMessage());
            return new StunResponse(sendChannel, stunRequest.getSocketAddress(), message);
        } catch (StunProtocolException e) {
            ErrorCodeAttribute errorAttribute = new ErrorCodeAttribute(e);
            message.getAttributes().put(errorAttribute.getCode(), errorAttribute);
            return new StunResponse(channel, stunRequest.getSocketAddress(), message);
        }
    }

    @SneakyThrows
    private Map<Integer, MessageAttribute> prepareAttributes(ServerType serverType, DatagramChannel channel, StunRequest stunRequest) {
        Map<Integer, MessageAttribute> attributes = new HashMap<>();
        attributes.put(
                KnownAttributeName.XOR_MAPPED_ADDRESS.getCode(),
                new AddressAttribute(KnownAttributeName.XOR_MAPPED_ADDRESS, stunRequest.getSocketAddress())
        );
        attributes.put(
                KnownAttributeName.MAPPED_ADDRESS.getCode(),
                new AddressAttribute(KnownAttributeName.MAPPED_ADDRESS, stunRequest.getSocketAddress())
        );
        attributes.put(
                KnownAttributeName.RESPONSE_ORIGIN.getCode(),
                new AddressAttribute(KnownAttributeName.RESPONSE_ORIGIN, channel.getLocalAddress())
        );

        ServerType otherServer = ServerType.getOther(serverType);
        DatagramChannel otherChannel = channelMap.get(otherServer);
        if (null != otherChannel) {
            attributes.put(
                    KnownAttributeName.OTHER_ADDRESS.getCode(),
                    new AddressAttribute(KnownAttributeName.OTHER_ADDRESS, otherChannel.getLocalAddress())
            );
        }
        return attributes;
    }

    private DatagramChannel getSenderChannel(ServerType serverType, Message message) {
        ChangeRequestAttribute changeRequestAttribute = (ChangeRequestAttribute) message.getAttributes()
                .get(KnownAttributeName.CHANGE_REQUEST.getCode());
        if (null == changeRequestAttribute) {
            return channelMap.get(serverType);
        }
        ServerType changedServer = ServerType.change(
                serverType,
                changeRequestAttribute.getChangeHost(),
                changeRequestAttribute.getChangePort()
        );
        return Optional.ofNullable(channelMap.get(changedServer))
                .orElseThrow(() -> new StunProtocolException(
                        "Server does not support RFC 5780 filtering test",
                        ErrorCode.UNKNOWN_ATTRIBUTE.getCode()
                ));
    }

}
