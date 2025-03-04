package com.gastonlagaf.udp.test;

import com.gastonlagaf.udp.client.discovery.InternetDiscovery;
import com.gastonlagaf.udp.client.model.ClientProperties;
import com.gastonlagaf.udp.test.protocol.PureProtocol;
import com.gastonlagaf.udp.turn.model.NatBehaviour;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public class PureSenderClient {

    public static void main(String[] args) throws Exception {
        InetAddress hostIp = InternetDiscovery.getAddress();
        ClientProperties clientProperties = new ClientProperties(
                new InetSocketAddress(hostIp, 40004),
                new InetSocketAddress(hostIp, 40001),
                new InetSocketAddress(hostIp, 3478),
                new InetSocketAddress(hostIp, 3478),
                5000L
        );

        PureProtocol pureProtocol = new PureProtocol(NatBehaviour.NO_NAT, clientProperties, false);
        pureProtocol.start(clientProperties.getHostAddress());

        InetSocketAddress socketAddress = new InetSocketAddress(hostIp, 40001);
        for (int i = 0; i < 100; i++) {
            String message = "Ping " + i;
            pureProtocol.getClient().send(socketAddress, message);
            Thread.sleep(2000L);
        }

        pureProtocol.close();
    }

}
