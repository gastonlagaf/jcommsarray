package com.gastonlagaf.udp.protocol;

import com.gastonlagaf.udp.protocol.model.UdpPacketHandlerResult;

import java.io.Closeable;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public interface Protocol<T> extends Closeable {

    T deserialize(InetSocketAddress receiverAddress, InetSocketAddress senderAddress, ByteBuffer buffer);

    ByteBuffer serialize(T packet);

    UdpPacketHandlerResult handle(InetSocketAddress receiverAddress, InetSocketAddress senderAddress, T packet);

    void start(InetSocketAddress... addresses);

}
