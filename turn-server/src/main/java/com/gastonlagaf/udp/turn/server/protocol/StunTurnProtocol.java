package com.gastonlagaf.udp.turn.server.protocol;

import com.gastonlagaf.udp.codec.CommunicationCodec;
import com.gastonlagaf.udp.protocol.Protocol;
import com.gastonlagaf.udp.protocol.model.UdpPacketHandlerResult;
import com.gastonlagaf.udp.socket.UdpChannelRegistry;
import com.gastonlagaf.udp.socket.UdpSockets;
import com.gastonlagaf.udp.turn.exception.StunProtocolException;
import com.gastonlagaf.udp.turn.integrity.IntegrityVerifier;
import com.gastonlagaf.udp.turn.model.*;
import com.gastonlagaf.udp.turn.server.codec.ServerMessageCodec;
import com.gastonlagaf.udp.turn.server.handler.StunMessageHandler;
import com.gastonlagaf.udp.turn.server.handler.impl.*;
import com.gastonlagaf.udp.turn.server.model.ContexedMessage;
import com.gastonlagaf.udp.turn.server.model.ServersDispatcher;
import com.gastonlagaf.udp.turn.server.model.StunResponse;
import com.gastonlagaf.udp.turn.server.model.StunServerProperties;
import com.gastonlagaf.udp.turn.server.turn.TurnSession;
import com.gastonlagaf.udp.turn.server.turn.TurnSessions;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;
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
                ? properties.getIpAddresses().stream().collect(Collectors.toMap(Function.identity(), it -> new TurnSessions()))
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
            return new UdpPacketHandlerResult();
        } catch (Exception e) {
            log.error("Error handling incoming message " + packet.getHeader().getType(), e);
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
