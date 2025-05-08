package com.gastonlagaf.udp.client.ice.protocol;

import com.gastonlagaf.udp.client.UdpClient;
import com.gastonlagaf.udp.client.model.ConnectResult;
import com.gastonlagaf.udp.client.ice.model.IceRole;
import com.gastonlagaf.udp.client.ice.model.IceSession;
import com.gastonlagaf.udp.client.model.ClientProperties;
import com.gastonlagaf.udp.client.protocol.BaseClientProtocol;
import com.gastonlagaf.udp.client.turn.TurnClientProtocol;
import com.gastonlagaf.udp.client.turn.proxy.TurnProxy;
import com.gastonlagaf.udp.protocol.ClientProtocol;
import com.gastonlagaf.udp.protocol.model.UdpPacketHandlerResult;
import com.gastonlagaf.udp.socket.UdpSockets;
import com.gastonlagaf.udp.turn.codec.impl.MessageCodec;
import com.gastonlagaf.udp.turn.exception.StunProtocolException;
import com.gastonlagaf.udp.turn.model.*;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class IceProtocol extends BaseClientProtocol<Message> {

    private final IceSession iceSession;

    private final CompletableFuture<ConnectResult<IceProtocol>> future;

    private final MessageCodec codec = new MessageCodec();

    public IceProtocol(UdpSockets sockets, IceSession iceSession, ClientProperties clientProperties, CompletableFuture<ConnectResult<IceProtocol>> future) {
        this(NatBehaviour.NO_NAT, iceSession, sockets, clientProperties, future);
    }

    public IceProtocol(NatBehaviour natBehaviour, IceSession iceSession, UdpSockets sockets, ClientProperties clientProperties, CompletableFuture<ConnectResult<IceProtocol>> future) {
        super(natBehaviour, clientProperties, sockets);
        this.iceSession = iceSession;
        this.future = future;
    }

    @Override
    protected String getCorrelationId(Message message) {
        return HexFormat.of().formatHex(message.getHeader().getTransactionId());
    }

    @Override
    protected UdpClient<Message> createUdpClient(UdpClient<Message> udpClient) {
        return udpClient;
    }

    @Override
    public Message deserialize(InetSocketAddress receiverAddress, InetSocketAddress senderAddress, ByteBuffer buffer) {
        return codec.decode(buffer);
    }

    @Override
    public ByteBuffer serialize(Message packet) {
        return codec.encode(packet);
    }

    @Override
    public UdpPacketHandlerResult handle(InetSocketAddress receiverAddress, InetSocketAddress senderAddress, Message packet) {
        String txId = HexFormat.of().formatHex(packet.getHeader().getTransactionId());
        if (!pendingMessages.complete(txId, packet) && MessageType.BINDING_REQUEST.equals(packet.getHeader().getType())) {
            boolean shouldNominate = packet.getAttributes().containsKey(KnownAttributeName.USE_CANDIDATE.getCode())
                    && IceRole.CONTROLLED.equals(iceSession.getRole());

            Boolean requestValid = respond(receiverAddress, senderAddress, packet);

            if (shouldNominate && requestValid) {
                Optional.ofNullable(future).ifPresent(it -> {
                    ConnectResult<IceProtocol> connectResult = new ConnectResult<>(senderAddress, this);
                    it.complete(connectResult);
                });
            }
        }
        return new UdpPacketHandlerResult();
    }

    private Boolean respond(InetSocketAddress receiverAddress, InetSocketAddress senderAddress, Message packet) {
        IceRole role = packet.getAttributes().containsKey(KnownAttributeName.ICE_CONTROLLING.getCode())
                ? IceRole.CONTROLLING
                : IceRole.CONTROLLED;
        boolean result = !role.equals(iceSession.getRole());
        Message message;
        if (!result) {
            StunProtocolException error = new StunProtocolException("Role conflict", ErrorCode.ROLE_CONFLICT.getCode());
            message = new Message(MessageType.BINDING_RESPONSE, packet.getHeader().getTransactionId(), error);
            future.completeExceptionally(error);
        } else {
            Map<Integer, MessageAttribute> attributes = prepareAttributes(receiverAddress, senderAddress);
            MessageHeader messageHeader = new MessageHeader(
                    MessageType.BINDING_RESPONSE.getCode(), 0, packet.getHeader().getTransactionId()
            );
            message = new Message(messageHeader, attributes);
        }

        client.send(receiverAddress, senderAddress, message);

        return result;
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
        return attributes;
    }

}
