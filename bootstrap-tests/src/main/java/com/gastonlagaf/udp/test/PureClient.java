package com.gastonlagaf.udp.test;

import com.gastonlagaf.udp.client.stun.StunClientProtocol;
import com.gastonlagaf.udp.client.stun.client.StunClient;
import com.gastonlagaf.udp.discovery.InternetDiscovery;
import com.gastonlagaf.udp.client.model.ClientProperties;
import com.gastonlagaf.udp.socket.UdpSockets;
import com.gastonlagaf.udp.client.protocol.PureProtocol;
import com.gastonlagaf.udp.turn.model.NatBehaviour;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class PureClient {

    public static void main(String[] args) throws Exception {
        UdpSockets sockets = new UdpSockets(1);
        sockets.start();

        InetAddress hostIp = InternetDiscovery.getAddress();
        ClientProperties clientProperties = new ClientProperties(
                new InetSocketAddress(hostIp, 40001),
                new InetSocketAddress(hostIp, 40004),
                new InetSocketAddress(hostIp, 3478),
                new InetSocketAddress(hostIp, 3478),
                5000L
        );

        InetSocketAddress reflexiveAddress;
        try (StunClientProtocol stunClientProtocol = new StunClientProtocol(sockets, clientProperties)) {
            stunClientProtocol.start();
            reflexiveAddress = ((StunClient)stunClientProtocol.getClient()).getReflexiveAddress();
            System.out.println(reflexiveAddress);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        PureProtocol pureProtocol = new PureProtocol(sockets, NatBehaviour.NO_NAT, clientProperties, true);
        pureProtocol.start();
    }

}
