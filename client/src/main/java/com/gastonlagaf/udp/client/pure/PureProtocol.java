package com.gastonlagaf.pure;

import com.gastonlagaf.udp.client.BaseUdpClient;
import com.gastonlagaf.udp.client.UdpClient;
import com.gastonlagaf.udp.protocol.Protocol;
import com.gastonlagaf.udp.protocol.model.UdpPacketHandlerResult;
import com.gastonlagaf.udp.socket.UdpSockets;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

@Slf4j
public class PureProtocol implements Protocol<String> {

    private final UdpSockets<String> sockets;

    @Getter
    private final UdpClient<String> client;

    public PureProtocol(Integer workersCount) {
        this.sockets = new UdpSockets<>(workersCount);
        this.client = new BaseUdpClient<>(this.sockets);
        this.sockets.start(this);
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
        client.send(receiverAddress, senderAddress, packet + " Response");
        return new UdpPacketHandlerResult();
    }

    @Override
    public void start(InetSocketAddress... addresses) {

    }

    @Override
    public void close() throws IOException {
        this.sockets.close();
    }
}
