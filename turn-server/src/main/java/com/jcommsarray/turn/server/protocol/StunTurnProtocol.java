package com.jcommsarray.turn.server.protocol;

import com.jcommsarray.codec.CommunicationCodec;
import com.jcommsarray.test.protocol.Protocol;
import com.jcommsarray.test.protocol.model.UdpPacketHandlerResult;
import com.jcommsarray.test.socket.UdpChannelRegistry;
import com.jcommsarray.test.socket.UdpSockets;
import com.jcommsarray.turn.exception.StunProtocolException;
import com.jcommsarray.turn.model.*;
import com.jcommsarray.turn.server.codec.ServerMessageCodec;
import com.jcommsarray.turn.server.handler.StunMessageHandler;
import com.jcommsarray.turn.server.handler.impl.*;
import com.jcommsarray.turn.server.model.ContexedMessage;
import com.jcommsarray.turn.server.model.ServersDispatcher;
import com.jcommsarray.turn.server.model.StunResponse;
import com.jcommsarray.turn.server.model.StunServerProperties;
import com.jcommsarray.turn.server.turn.TurnSession;
import com.jcommsarray.turn.server.turn.TurnSessions;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class StunTurnProtocol implements Protocol<ContexedMessage> {

    private final CommunicationCodec<Message> messageCodec = new ServerMessageCodec();

    private final Map<MessageType, StunMessageHandler> handlers;

    private final Map<String, TurnSessions> turnSessionsMap;

    private final UdpSockets sockets;

    private final Collection<InetSocketAddress> hostAddresses;

    public StunTurnProtocol(UdpSockets udpSockets, StunServerProperties properties) {
        this.turnSessionsMap = properties.getEnableTurn()
                ? properties.getIpAddresses().stream().collect(Collectors.toMap(Function.identity(), it -> new TurnSessions(udpSockets)))
                : null;

        UdpChannelRegistry registry = udpSockets.getRegistry();
        ServersDispatcher serversDispatcher = new ServersDispatcher(properties.getServers());

        this.handlers = Map.of(
                MessageType.BINDING_REQUEST, new BindingRequestMessageHandler(serversDispatcher),
                MessageType.ALLOCATE, new AllocateMessageHandler(turnSessionsMap, null, registry, this),
                MessageType.REFRESH, new RefreshMessageHandler(turnSessionsMap),
                MessageType.CREATE_PERMISSION, new CreatePermissionRequestHandler(),
                MessageType.SEND, new SendMessageHandler(),
                MessageType.DATA, new DataMessageHandler(),
                MessageType.CHANNEL_BIND, new ChannelBindMessageHandler(),
                MessageType.OUTBOUND_CHANNEL_DATA, new OutboundChannelDataMessageHandler(),
                MessageType.INBOUND_CHANNEL_DATA, new InboundChannelDataMessageHandler()
        );

        this.sockets = udpSockets;
        this.hostAddresses = properties.getServers().values();
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
    public UdpPacketHandlerResult handle(InetSocketAddress receiverAddress, InetSocketAddress senderAddress, ByteBuffer buffer) {
        ContexedMessage message = deserialize(receiverAddress, senderAddress, buffer);
        return handle(receiverAddress, senderAddress, message);
    }

    public UdpPacketHandlerResult handle(InetSocketAddress receiverAddress, InetSocketAddress senderAddress, ContexedMessage packet) {
        try {
            StunResponse response = handlers.get(packet.getHeader().getType()).handle(receiverAddress, senderAddress, packet);
            Optional.ofNullable(response)
                    .flatMap(it -> Optional.ofNullable(it.getMessage()))
                    .ifPresent(it -> {
                        ByteBuffer data = messageCodec.encode(response.getMessage());
                        sockets.send(response.getSourceAddress(), response.getTargetAddress(), data);
                    });
            return new UdpPacketHandlerResult(response.getCloseChannel());
        } catch (StunProtocolException spe) {
            Message errorMessage = new Message(packet.getHeader().getType(), packet.getHeader().getTransactionId(), spe);
            ByteBuffer serializedErrorMessage = messageCodec.encode(errorMessage);
            sockets.send(receiverAddress, senderAddress, serializedErrorMessage);
            log.error("", spe);
            return new UdpPacketHandlerResult();
        } catch (Exception e) {
            log.error("Error handling incoming message {}", packet.getHeader().getType(), e);
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
    public void start() {
        this.hostAddresses.forEach(it -> this.sockets.getRegistry().register(it, this));
    }

    @Override
    public void close() throws IOException {
        this.sockets.close();
    }

}
