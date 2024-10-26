package com.gastonlagaf.pure;

import com.gastonlagaf.udp.UdpPacketHandler;
import com.gastonlagaf.udp.model.UdpPacketHandlerResult;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

@Slf4j
public class PurePacketHandler implements UdpPacketHandler<byte[]> {

    @Override
    public byte[] deserialize(InetSocketAddress receiverAddress, InetSocketAddress senderAddress, ByteBuffer buffer) {
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return bytes;
    }

    @Override
    public UdpPacketHandlerResult handle(InetSocketAddress receiverAddress, InetSocketAddress senderAddress, byte[] packet) {
        String message = new String(packet);
        log.info("Received message: {}", message);
        byte[] response = (message + " Response").getBytes();
        return new UdpPacketHandlerResult(receiverAddress, senderAddress, response);
    }

}
