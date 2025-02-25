package com.gastonlagaf.udp.socket;

import java.io.Closeable;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;

public interface ChannelRegistry extends Closeable {

    SelectionKey register(InetSocketAddress inetSocketAddress);

    void deregister(SelectionKey channel);

}
