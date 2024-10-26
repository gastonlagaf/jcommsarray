package com.gastonlagaf.udp;

import com.gastonlagaf.udp.model.UdpPacketHandlerResult;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public interface UdpPacketHandler<T> {

    T deserialize(InetSocketAddress receiverAddress, InetSocketAddress senderAddress, ByteBuffer buffer);

    UdpPacketHandlerResult handle(InetSocketAddress receiverAddress, InetSocketAddress senderAddress, T packet);

}
