package com.gastonlagaf.udp.test.protocol;

import com.gastonlagaf.udp.client.UdpClient;
import com.gastonlagaf.udp.client.model.ClientProperties;
import com.gastonlagaf.udp.client.protocol.BaseClientProtocol;
import com.gastonlagaf.udp.protocol.model.UdpPacketHandlerResult;
import com.gastonlagaf.udp.socket.UdpSockets;
import com.gastonlagaf.udp.turn.model.NatBehaviour;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

@Slf4j
public class PureProtocol extends BaseClientProtocol<String> {

    private final Boolean shouldReply;

    public PureProtocol(UdpSockets udpSockets, NatBehaviour natBehaviour, ClientProperties clientProperties, Boolean shouldReply) {
        super(natBehaviour, clientProperties, udpSockets);
        this.shouldReply = shouldReply;
    }

    @Override
    protected String getCorrelationId(String message) {
        return "";
    }

    @Override
    protected UdpClient<String> createUdpClient(UdpClient<String> udpClient) {
        return udpClient;
    }

    @Override
    public String deserialize(InetSocketAddress receiverAddress, InetSocketAddress senderAddress, ByteBuffer buffer) {
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return new String(bytes);
    }

    @Override
    public ByteBuffer serialize(String packet) {
        return ByteBuffer.wrap(packet.getBytes());
    }

    @Override
    public UdpPacketHandlerResult handle(InetSocketAddress receiverAddress, InetSocketAddress senderAddress, String packet) {
        log.info("Received message: {}", packet);
        if (shouldReply) {
            client.send(receiverAddress, senderAddress, packet + " Response");
        }
        return new UdpPacketHandlerResult();
    }
}
