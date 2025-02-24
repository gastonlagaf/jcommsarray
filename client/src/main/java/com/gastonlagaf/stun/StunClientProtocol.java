package com.gastonlagaf.stun;

import com.gastonlagaf.stun.client.StunClient;
import com.gastonlagaf.stun.client.impl.UdpStunClient;
import com.gastonlagaf.stun.client.model.StunClientProperties;
import com.gastonlagaf.stun.codec.impl.MessageCodec;
import com.gastonlagaf.stun.model.Message;
import com.gastonlagaf.udp.client.PendingMessages;
import com.gastonlagaf.udp.client.UdpClient;
import com.gastonlagaf.udp.codec.CommunicationCodec;
import com.gastonlagaf.udp.protocol.ClientProtocol;
import com.gastonlagaf.udp.protocol.model.UdpPacketHandlerResult;
import com.gastonlagaf.udp.socket.UdpSockets;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HexFormat;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class StunClientProtocol implements ClientProtocol<Message> {

    private static final Integer WORKERS_COUNT = 1;

    private final CommunicationCodec<Message> codec = new MessageCodec();

    private final PendingMessages<Message> pendingMessages;

    private final UdpSockets<Message> sockets;

    private final StunClient client;

    public StunClientProtocol(StunClientProperties properties, Long socketTimeoutMillis) {
        this.pendingMessages = new PendingMessages<>(socketTimeoutMillis);
        this.sockets = new UdpSockets<>(WORKERS_COUNT);
        this.client = new UdpStunClient(properties, sockets, this);
        this.sockets.start(this);
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
        String txId = HexFormat.of().formatHex(packet.getHeader().getTransactionId());
        pendingMessages.complete(txId, packet);
        return null;
    }

    @Override
    public UdpClient<Message> getClient() {
        return client;
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
