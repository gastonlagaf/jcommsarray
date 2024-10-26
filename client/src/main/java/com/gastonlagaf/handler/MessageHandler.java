package com.gastonlagaf.handler;

import com.gastonlagaf.udp.codec.CommunicationCodec;
import com.gastonlagaf.stun.codec.impl.MessageCodec;
import com.gastonlagaf.stun.model.Message;
import com.gastonlagaf.stun.model.MessageType;
import com.gastonlagaf.udp.model.UdpPacketHandlerResult;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HexFormat;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class MessageHandler implements ClientMessageHandler<Message, byte[]> {

    private static final Set<MessageType> RESPONSE_MESSAGE_TYPES = Set.of(
            MessageType.BINDING_REQUEST,
            MessageType.CHANNEL_BIND,
            MessageType.CREATE_PERMISSION,
            MessageType.ALLOCATE,
            MessageType.REFRESH
    );

    private final CommunicationCodec<Message> messageCodec = new MessageCodec();

    private final PendingMessages pendingMessages;

    private final MessageConsumer messageConsumer;

    public MessageHandler(Long socketTimeoutMillis) {
        this(socketTimeoutMillis, null);
    }

    public MessageHandler(Long socketTimeoutMillis, MessageConsumer messageConsumer) {
        this.pendingMessages = new PendingMessages(socketTimeoutMillis);
        this.messageConsumer = messageConsumer;
    }

    @Override
    public Message deserialize(InetSocketAddress receiverAddress, InetSocketAddress senderAddress, ByteBuffer buffer) {
        return messageCodec.decode(buffer);
    }

    @Override
    public UdpPacketHandlerResult handle(InetSocketAddress receiverAddress, InetSocketAddress senderAddress, Message message) {
        if (RESPONSE_MESSAGE_TYPES.contains(message.getHeader().getType())) {
            String txId = HexFormat.of().formatHex(message.getHeader().getTransactionId());
            pendingMessages.complete(txId, message);
        } else {
            Optional.ofNullable(messageConsumer).ifPresent(it -> it.handle(receiverAddress, senderAddress, message));
        }
        return null;
    }

    @Override
    public CompletableFuture<Message> awaitResult(byte[] txId) {
        String txIdAsString = HexFormat.of().formatHex(txId);
        return pendingMessages.put(txIdAsString);
    }

}
