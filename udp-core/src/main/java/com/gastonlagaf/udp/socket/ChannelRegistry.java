package com.gastonlagaf.udp.socket;

import java.io.Closeable;
import java.net.InetSocketAddress;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;

public interface ChannelRegistry extends Closeable {

    SelectionKey register(InetSocketAddress inetSocketAddress);

    SelectionKey attach(Channel channel);

    void deregister(SelectionKey key);

    Channel detach(SelectionKey key);

}
