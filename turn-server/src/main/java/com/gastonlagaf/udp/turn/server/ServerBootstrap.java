package com.gastonlagaf.udp.turn.server;

import com.gastonlagaf.udp.discovery.InternetDiscovery;
import com.gastonlagaf.udp.socket.UdpSockets;
import com.gastonlagaf.udp.turn.server.model.StunInstanceProperties;
import com.gastonlagaf.udp.turn.server.model.StunServerProperties;
import com.gastonlagaf.udp.turn.server.protocol.StunTurnProtocol;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public class ServerBootstrap {

    public static void main(String[] args) {
        UdpSockets sockets = new UdpSockets(1);
        sockets.start();
        InetAddress address = InternetDiscovery.getAddress();
        StunServerProperties stunServerProperties = new StunServerProperties(
                new StunInstanceProperties(address.getHostAddress(), 3478, 3479),
                null, true, 1
        );
        StunTurnProtocol protocol = new StunTurnProtocol(sockets, stunServerProperties);
        protocol.start();
    }

}
