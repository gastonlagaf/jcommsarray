package com.gastonlagaf.udp.turn.server;

import com.gastonlagaf.udp.turn.server.model.StunInstanceProperties;
import com.gastonlagaf.udp.turn.server.model.StunServerProperties;
import com.gastonlagaf.udp.turn.server.protocol.StunTurnProtocol;

import java.net.InetSocketAddress;

public class ServerBootstrap {

    public static void main(String[] args) {
        StunServerProperties stunServerProperties = new StunServerProperties(
                new StunInstanceProperties("192.168.0.109", 3478, 3479),
                null, true, 1
        );
        StunTurnProtocol protocol = new StunTurnProtocol(stunServerProperties);
        protocol.start(
                stunServerProperties.getServers().values().toArray(InetSocketAddress[]::new)
        );
    }

}
