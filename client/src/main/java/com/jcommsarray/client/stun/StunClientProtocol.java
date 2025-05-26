package com.jcommsarray.client.stun;

import com.jcommsarray.client.UdpClient;
import com.jcommsarray.client.model.ClientProperties;
import com.jcommsarray.client.protocol.TurnAwareClientProtocol;
import com.jcommsarray.client.stun.client.impl.UdpStunClient;
import com.jcommsarray.codec.CommunicationCodec;
import com.jcommsarray.test.protocol.model.UdpPacketHandlerResult;
import com.jcommsarray.test.socket.UdpSockets;
import com.jcommsarray.turn.codec.impl.MessageCodec;
import com.jcommsarray.turn.model.Message;
import com.jcommsarray.turn.model.NatBehaviour;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HexFormat;

public class StunClientProtocol extends TurnAwareClientProtocol<Message> {

    private final CommunicationCodec<Message> codec = new MessageCodec();

    public StunClientProtocol(UdpSockets udpSockets, ClientProperties clientProperties) {
        super(NatBehaviour.NO_NAT, clientProperties, udpSockets);
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
