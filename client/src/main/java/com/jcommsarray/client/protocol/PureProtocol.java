package com.jcommsarray.client.protocol;

import com.jcommsarray.client.UdpClient;
import com.jcommsarray.client.model.ClientProperties;
import com.jcommsarray.test.protocol.model.UdpPacketHandlerResult;
import com.jcommsarray.test.socket.UdpSockets;
import com.jcommsarray.turn.model.NatBehaviour;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

@Slf4j
public class PureProtocol extends TurnAwareClientProtocol<String> {

    private static final String RESPONSE_APPENDIX = " Response";

    private final Boolean shouldReply;

    public PureProtocol(UdpSockets udpSockets, NatBehaviour natBehaviour, ClientProperties clientProperties, Boolean shouldReply) {
        super(natBehaviour, clientProperties, udpSockets);
        this.shouldReply = shouldReply;
    }

    public PureProtocol(TurnAwareClientProtocol<?> baseProtocol, Boolean shouldReply) {
        super(baseProtocol);
        this.shouldReply = shouldReply;
    }

    @Override
    protected String getCorrelationId(String message) {
        return message;
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
            client.send(receiverAddress, senderAddress, packet + RESPONSE_APPENDIX);
        } else {
            int appendixIndex = packet.indexOf(RESPONSE_APPENDIX);
            if (-1 != appendixIndex) {
                String correlationId = packet.substring(0, appendixIndex);
                pendingMessages.complete(correlationId, packet);
            }
        }
        return new UdpPacketHandlerResult();
    }
}
