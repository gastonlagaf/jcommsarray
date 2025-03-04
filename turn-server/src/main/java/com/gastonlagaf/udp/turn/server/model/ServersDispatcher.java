package com.gastonlagaf.udp.turn.server.model;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class ServersDispatcher {

    private final Map<InetSocketAddress, ServerType> serverTypeMap = new HashMap<>();

    private final Map<ServerType, InetSocketAddress> addressMap = new HashMap<>();

    public ServersDispatcher(Map<ServerType, InetSocketAddress> serverTypeMap) {
        serverTypeMap.forEach((key, value) -> {
            this.serverTypeMap.put(value, key);
            this.addressMap.put(key, value);
        });
    }

    public ServerType getServerType(InetSocketAddress address) {
        return serverTypeMap.get(address);
    }

    public InetSocketAddress getAddress(ServerType serverType) {
        return addressMap.get(serverType);
    }

}
