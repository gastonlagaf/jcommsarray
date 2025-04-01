package com.gastonlagaf.udp.socket;

import com.gastonlagaf.udp.protocol.Protocol;

import java.io.Closeable;
import java.net.InetSocketAddress;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;

public interface ChannelRegistry extends Closeable {

    SelectionKey register(InetSocketAddress inetSocketAddress, Protocol<?> protocol);

    SelectionKey switchProtocol(SelectionKey key, Protocol<?> protocol);

    void deregister(SelectionKey key);

}
