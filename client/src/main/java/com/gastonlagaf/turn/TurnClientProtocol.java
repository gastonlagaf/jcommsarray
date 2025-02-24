package com.gastonlagaf.turn;

import com.gastonlagaf.stun.codec.impl.MessageCodec;
import com.gastonlagaf.stun.model.*;
import com.gastonlagaf.udp.client.PendingMessages;
import com.gastonlagaf.udp.client.UdpClient;
import com.gastonlagaf.udp.codec.CommunicationCodec;
import com.gastonlagaf.udp.protocol.ClientProtocol;
import com.gastonlagaf.udp.protocol.model.UdpPacketHandlerResult;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class TurnClientProtocol<T> implements ClientProtocol<Message> {

    private static final Set<MessageType> RESPONSE_MESSAGE_TYPES = Set.of(
            MessageType.BINDING_REQUEST,
            MessageType.CHANNEL_BIND,
            MessageType.CREATE_PERMISSION,
            MessageType.ALLOCATE,
            MessageType.REFRESH
    );

    private final CommunicationCodec<Message> codec = new MessageCodec();

    private final Map<Integer, InetSocketAddress> channelBindings = new HashMap<>();

    private final PendingMessages<Message> pendingMessages;

    private final ClientProtocol<T> targetProtocol;

    public TurnClientProtocol(ClientProtocol<T> targetProtocol, Long socketTimeoutMillis) {
        this.pendingMessages = new PendingMessages<>(socketTimeoutMillis);
        this.targetProtocol = targetProtocol;
    }

    @Override
    public CompletableFuture<Message> awaitResult(Message message) {
        String txId = HexFormat.of().formatHex(message.getHeader().getTransactionId());
        return pendingMessages.put(txId);
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
        if (RESPONSE_MESSAGE_TYPES.contains(packet.getHeader().getType())) {
            String txId = HexFormat.of().formatHex(packet.getHeader().getTransactionId());
            pendingMessages.complete(txId, packet);
            return null;
        }
        InetSocketAddress actualSender = extractSender(packet);
        if (null == actualSender) {
            return null;
        }
        byte[] data = packet.getAttributes().<DefaultMessageAttribute>get(KnownAttributeName.DATA).getValue();
        T message = targetProtocol.deserialize(receiverAddress, actualSender, ByteBuffer.wrap(data));
        targetProtocol.handle(receiverAddress, actualSender, message);
        return null;
    }

    @Override
    public UdpClient<Message> getClient() {
        throw new UnsupportedOperationException();
    }

    private InetSocketAddress extractSender(Message message) {
        return switch (message.getHeader().getType()) {
            case DATA -> message.getAttributes().<AddressAttribute>get(KnownAttributeName.XOR_PEER_ADDRESS).toInetSocketAddress();
            case INBOUND_CHANNEL_DATA -> {
                Integer channelNumber = message.getAttributes().<ChannelNumberAttribute>get(KnownAttributeName.CHANNEL_NUMBER).getValue();
                yield channelBindings.get(channelNumber);
            }
            default -> null;
        };
    }

    @Override
    public void start(InetSocketAddress... addresses) {
        // No-op
    }

    @Override
    public void close() throws IOException {
        // No-op
    }
}
