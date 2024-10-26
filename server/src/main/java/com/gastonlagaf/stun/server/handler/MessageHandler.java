package com.gastonlagaf.stun.server.handler;

import com.gastonlagaf.udp.codec.CommunicationCodec;
import com.gastonlagaf.stun.exception.StunProtocolException;
import com.gastonlagaf.stun.integrity.IntegrityVerifier;
import com.gastonlagaf.stun.model.*;
import com.gastonlagaf.stun.server.codec.ServerMessageCodec;
import com.gastonlagaf.stun.server.handler.impl.*;
import com.gastonlagaf.stun.server.model.ContexedMessage;
import com.gastonlagaf.stun.server.model.ServerDispatcher;
import com.gastonlagaf.stun.server.model.StunResponse;
import com.gastonlagaf.stun.server.turn.TurnSession;
import com.gastonlagaf.stun.server.turn.TurnSessions;
import com.gastonlagaf.udp.ChannelRegistry;
import com.gastonlagaf.udp.UdpPacketHandler;
import com.gastonlagaf.udp.model.UdpPacketHandlerResult;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class MessageHandler implements UdpPacketHandler<ContexedMessage> {

    private final CommunicationCodec<Message> messageCodec = new ServerMessageCodec();

    private final Map<MessageType, StunMessageHandler> handlers;

    private final Map<String, TurnSessions> turnSessionsMap;

    public MessageHandler(ServerDispatcher serverDispatcher, Map<String, TurnSessions> turnSessionsMap, IntegrityVerifier integrityVerifier, ChannelRegistry channelRegistry) {
        this.turnSessionsMap = turnSessionsMap;
        this.handlers = Map.of(
                MessageType.BINDING_REQUEST, new BindingRequestMessageHandler(serverDispatcher),
                MessageType.ALLOCATE, new AllocateMessageHandler(turnSessionsMap, integrityVerifier, channelRegistry),
                MessageType.REFRESH, new RefreshMessageHandler(turnSessionsMap),
                MessageType.CREATE_PERMISSION, new CreatePermissionRequestHandler(),
                MessageType.SEND, new SendMessageHandler(),
                MessageType.DATA, new DataMessageHandler(),
                MessageType.CHANNEL_BIND, new ChannelBindMessageHandler(),
                MessageType.OUTBOUND_CHANNEL_DATA, new OutboundChannelDataMessageHandler(),
                MessageType.INBOUND_CHANNEL_DATA, new InboundChannelDataMessageHandler()
        );
    }

    @Override
    public ContexedMessage deserialize(InetSocketAddress receiverAddress, InetSocketAddress senderAddress, ByteBuffer buffer) {
        TurnSession turnSession = Optional.ofNullable(turnSessionsMap)
                .flatMap(it -> Optional.ofNullable(it.get(receiverAddress.getAddress().getHostAddress())))
                .map(it -> it.get(receiverAddress))
                .orElse(null);

        Message message = doDeserialize(turnSession, senderAddress, buffer);

        return new ContexedMessage(message, turnSession);
    }

    @Override
    public UdpPacketHandlerResult handle(InetSocketAddress receiverAddress, InetSocketAddress senderAddress, ContexedMessage packet) {
        try {
            StunResponse response = handlers.get(packet.getHeader().getType()).handle(receiverAddress, senderAddress, packet);
            byte[] data = messageCodec.encode(response.getMessage()).array();
            return new UdpPacketHandlerResult(response.getSourceAddress(), response.getTargetAddress(), data, response.getCloseChannel());
        } catch (StunProtocolException spe) {
            Message errorMessage = new Message(packet.getHeader().getType(), packet.getHeader().getTransactionId(), spe);
            ByteBuffer serializedErrorMessage = messageCodec.encode(errorMessage);
            return new UdpPacketHandlerResult(receiverAddress, senderAddress, serializedErrorMessage.array());
        } catch (Exception e) {
            log.error("Error handling incoming message", e);
            return null;
        }
    }

    private Message doDeserialize(TurnSession turnSession, InetSocketAddress senderAddress, ByteBuffer buffer) {
        if (null == turnSession || senderAddress.equals(turnSession.getClientAddress())) {
            return messageCodec.decode(buffer);
        }
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);

        MessageHeader messageHeader = new MessageHeader(MessageType.DATA);
        Map<Integer, MessageAttribute> attributes = Map.of(
                KnownAttributeName.DATA.getCode(), new DefaultMessageAttribute(KnownAttributeName.DATA.getCode(), data.length, data)
        );
        return new Message(messageHeader, attributes);
    }

}
