package com.jcommsarray.turn.server;

import com.jcommsarray.test.discovery.InternetDiscovery;
import com.jcommsarray.test.socket.UdpSockets;
import com.jcommsarray.turn.server.model.StunInstanceProperties;
import com.jcommsarray.turn.server.model.StunServerProperties;
import com.jcommsarray.turn.server.protocol.StunTurnProtocol;

import java.net.InetAddress;

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
