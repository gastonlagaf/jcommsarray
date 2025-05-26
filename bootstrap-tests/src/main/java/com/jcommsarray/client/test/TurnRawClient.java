package com.jcommsarray.client.test;

import com.jcommsarray.test.discovery.InternetDiscovery;
import com.jcommsarray.client.model.ClientProperties;
import com.jcommsarray.test.socket.UdpSockets;
import com.jcommsarray.client.protocol.PureProtocol;
import com.jcommsarray.turn.model.NatBehaviour;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public class TurnRawClient {

    public static void main(String[] args) throws Exception {
        UdpSockets sockets = new UdpSockets(1);
        sockets.start();

        InetAddress hostIp = InternetDiscovery.getAddress();
        ClientProperties clientProperties = new ClientProperties(
                new InetSocketAddress(hostIp, 40003),
                new InetSocketAddress(hostIp, 40001),
                new InetSocketAddress(hostIp, 3478),
                new InetSocketAddress(hostIp, 3478),
                5000L
        );

        PureProtocol pureProtocol = new PureProtocol(sockets, NatBehaviour.ADDRESS_DEPENDENT, clientProperties, false);
        pureProtocol.start();

        InetSocketAddress socketAddress = new InetSocketAddress(hostIp, 40001);
        for (int i = 0; i < 100; i++) {
            String message = "Ping " + i;
            pureProtocol.getClient().send(socketAddress, message);
            Thread.sleep(2000L);
        }

        pureProtocol.close();
    }

}
