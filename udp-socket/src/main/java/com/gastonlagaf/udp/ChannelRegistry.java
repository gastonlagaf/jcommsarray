package com.gastonlagaf.udp;

import java.io.Closeable;
import java.nio.channels.SelectionKey;

public interface ChannelRegistry extends Closeable {

    SelectionKey register(String interfaceIp, Integer port);

    void deregister(SelectionKey channel);

}
