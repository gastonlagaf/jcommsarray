package com.gastonlagaf.udp.client.stun.server.protocol;

import com.gastonlagaf.udp.client.stun.exception.StunProtocolException;
import com.gastonlagaf.udp.client.stun.integrity.IntegrityVerifier;
import com.gastonlagaf.udp.client.stun.model.*;
import com.gastonlagaf.udp.client.stun.server.codec.ServerMessageCodec;
import com.gastonlagaf.udp.client.stun.server.handler.StunMessageHandler;
import com.gastonlagaf.udp.client.stun.server.handler.impl.*;
import com.gastonlagaf.udp.client.stun.server.model.ContexedMessage;
import com.gastonlagaf.udp.client.stun.server.model.ServerDispatcher;
import com.gastonlagaf.udp.client.stun.server.model.StunResponse;
import com.gastonlagaf.udp.client.stun.server.turn.TurnSession;
import com.gastonlagaf.udp.client.stun.server.turn.TurnSessions;
import com.gastonlagaf.udp.codec.CommunicationCodec;
import com.gastonlagaf.udp.protocol.Protocol;
import com.gastonlagaf.udp.protocol.model.UdpPacketHandlerResult;
import com.gastonlagaf.udp.socket.UdpChannelRegistry;
import com.gastonlagaf.udp.socket.UdpSockets;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class StunTurnProtocol implements Protocol<ContexedMessage> {

    private final CommunicationCodec<Message> messageCodec = new ServerMessageCodec();

    private final Map<MessageType, StunMessageHandler> handlers;

    private final Map<String, TurnSessions> turnSessionsMap;

    private final UdpSockets<ContexedMessage> sockets;

    public StunTurnProtocol(ServerDispatcher serverDispatcher, Map<String, TurnSessions> turnSessionsMap, IntegrityVerifier integrityVerifier, Integer workersCount) {
        this.turnSessionsMap = turnSessionsMap;

        UdpChannelRegistry registry = new UdpChannelRegistry(workersCount);

        this.handlers = Map.of(
                MessageType.BINDING_REQUEST, new BindingRequestMessageHandler(serverDispatcher),
                MessageType.ALLOCATE, new AllocateMessageHandler(turnSessionsMap, integrityVerifier, registry),
                MessageType.REFRESH, new RefreshMessageHandler(turnSessionsMap),
                MessageType.CREATE_PERMISSION, new CreatePermissionRequestHandler(),
                MessageType.SEND, new SendMessageHandler(),
                MessageType.DATA, new DataMessageHandler(),
                MessageType.CHANNEL_BIND, new ChannelBindMessageHandler(),
                MessageType.OUTBOUND_CHANNEL_DATA, new OutboundChannelDataMessageHandler(),
                MessageType.INBOUND_CHANNEL_DATA, new InboundChannelDataMessageHandler()
        );

        this.sockets = new UdpSockets<>(registry);
        this.sockets.start(this);
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
    public ByteBuffer serialize(ContexedMessage packet) {
        return messageCodec.encode(packet);
    }

    @Override
    public UdpPacketHandlerResult handle(InetSocketAddress receiverAddress, InetSocketAddress senderAddress, ContexedMessage packet) {
        try {
            StunResponse response = handlers.get(packet.getHeader().getType()).handle(receiverAddress, senderAddress, packet);
            ByteBuffer data = messageCodec.encode(response.getMessage());
            sockets.send(response.getSourceAddress(), response.getTargetAddress(), data);
            return new UdpPacketHandlerResult(response.getCloseChannel());
        } catch (StunProtocolException spe) {
            Message errorMessage = new Message(packet.getHeader().getType(), packet.getHeader().getTransactionId(), spe);
            ByteBuffer serializedErrorMessage = messageCodec.encode(errorMessage);
            sockets.send(receiverAddress, senderAddress, serializedErrorMessage);
            return new UdpPacketHandlerResult();
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

    @Override
    public void start(InetSocketAddress... addresses) {
        if (null == addresses) {
            return;
        }
        for (InetSocketAddress address : addresses) {
            this.sockets.getRegistry().register(address);
        }
        this.sockets.start(this);
    }

    @Override
    public void close() throws IOException {
        this.sockets.close();
    }

}
