package com.gastonlagaf.udp.client.stun;

import com.gastonlagaf.udp.client.UdpClient;
import com.gastonlagaf.udp.client.model.ClientProperties;
import com.gastonlagaf.udp.client.protocol.BaseClientProtocol;
import com.gastonlagaf.udp.client.stun.client.impl.UdpStunClient;
import com.gastonlagaf.udp.codec.CommunicationCodec;
import com.gastonlagaf.udp.protocol.model.UdpPacketHandlerResult;
import com.gastonlagaf.udp.turn.codec.impl.MessageCodec;
import com.gastonlagaf.udp.turn.model.Message;
import com.gastonlagaf.udp.turn.model.NatBehaviour;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HexFormat;

public class StunClientProtocol extends BaseClientProtocol<Message> {

    private static final Integer WORKERS_COUNT = 1;

    private final CommunicationCodec<Message> codec = new MessageCodec();

    public StunClientProtocol(ClientProperties clientProperties) {
        super(NatBehaviour.NO_NAT, clientProperties, WORKERS_COUNT);
    }

    @Override
    protected String getCorrelationId(Message message) {
        return HexFormat.of().formatHex(message.getHeader().getTransactionId());
    }

    @Override
    protected UdpClient<Message> createUdpClient(UdpClient<Message> udpClient) {
        return new UdpStunClient(udpClient, clientProperties);
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

}
