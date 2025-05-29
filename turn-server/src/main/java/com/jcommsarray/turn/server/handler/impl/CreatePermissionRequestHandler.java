package com.jcommsarray.turn.server.handler.impl;

import com.jcommsarray.turn.exception.StunProtocolException;
import com.jcommsarray.turn.model.*;
import com.jcommsarray.turn.server.handler.StunMessageHandler;
import com.jcommsarray.turn.server.model.ContexedMessage;
import com.jcommsarray.turn.server.model.StunResponse;
import lombok.RequiredArgsConstructor;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class CreatePermissionRequestHandler implements StunMessageHandler {

    @Override
    public MessageType getMessageType() {
        return MessageType.CREATE_PERMISSION;
    }

    @Override
    public StunResponse handle(InetSocketAddress serverAddress, InetSocketAddress clientAddress, ContexedMessage message) {
        if (null == message.getTurnSession()) {
            throw new StunProtocolException("Turn session has not been allocated", ErrorCode.ALLOCATION_MISMATCH.getCode());
        }
        List<AddressAttribute> peersList = message.getAttributes().findAll(KnownAttributeName.XOR_PEER_ADDRESS);
        if (null == peersList) {
            throw new StunProtocolException("None peers provided", ErrorCode.BAD_REQUEST.getCode());
        }
        List<AddressAttribute> invalidPeers = peersList.stream()
                .filter(it -> !IpFamily.IPV4.equals(it.getIpFamily()))
                .toList();
        if (!invalidPeers.isEmpty()) {
            throw new StunProtocolException("Some peers has different address family", ErrorCode.PEER_ADDRESS_FAMILY_MISMATCH.getCode());
        }
        peersList.forEach(it -> message.getTurnSession().getPeers().add(it.toInetSocketAddress()));

        MessageHeader header = new MessageHeader(MessageType.CREATE_PERMISSION.getCode(), 0, message.getHeader().getTransactionId());
        Message responseMessage = new Message(header, Map.of());
        return new StunResponse(serverAddress, clientAddress, responseMessage);
    }

}
