package com.jcommsarray.test;

import com.jcommsarray.client.model.ClientProperties;
import com.jcommsarray.client.protocol.PureProtocol;
import com.jcommsarray.client.stun.StunClientProtocol;
import com.jcommsarray.client.stun.client.StunClient;
import com.jcommsarray.test.discovery.InternetDiscovery;
import com.jcommsarray.test.socket.UdpSockets;
import com.jcommsarray.turn.model.NatBehaviour;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class PureSenderClient {

    public static void main(String[] args) throws Exception {
        UdpSockets sockets = new UdpSockets(1);
        sockets.start();

        InetAddress hostIp = InternetDiscovery.getAddress();
        ClientProperties clientProperties = new ClientProperties(
                new InetSocketAddress(hostIp, 40004),
                new InetSocketAddress(hostIp, 40001),
                new InetSocketAddress(hostIp, 3478),
                new InetSocketAddress(hostIp, 3478),
                5000L
        );

        InetSocketAddress reflexiveAddress;
        try (StunClientProtocol stunClientProtocol = new StunClientProtocol(sockets, clientProperties)) {
            stunClientProtocol.start();
            reflexiveAddress = ((StunClient) stunClientProtocol.getClient()).getReflexiveAddress();
            System.out.println(reflexiveAddress);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        PureProtocol pureProtocol = new PureProtocol(sockets, NatBehaviour.NO_NAT, clientProperties, false);
        pureProtocol.start();

        InetSocketAddress socketAddress = new InetSocketAddress(hostIp, 40001);
        for (int i = 0; i < 100; i++) {
            String message = "Ping " + i;
            pureProtocol.getClient().send(socketAddress, message);
            Thread.sleep(1000L);
        }

        pureProtocol.close();
    }

}
