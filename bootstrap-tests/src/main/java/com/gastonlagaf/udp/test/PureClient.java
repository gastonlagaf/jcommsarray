package com.gastonlagaf.udp.test;

import com.gastonlagaf.udp.client.discovery.InternetDiscovery;
import com.gastonlagaf.udp.client.model.ClientProperties;
import com.gastonlagaf.udp.test.protocol.PureProtocol;
import com.gastonlagaf.udp.turn.model.NatBehaviour;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public class PureClient {

    public static void main(String[] args) {
        InetAddress hostIp = InternetDiscovery.getAddress();
        ClientProperties clientProperties = new ClientProperties(
                new InetSocketAddress(hostIp, 40001),
                null, null, null, 5000L
        );

        PureProtocol pureProtocol = new PureProtocol(NatBehaviour.NO_NAT, clientProperties);
        pureProtocol.start(clientProperties.getHostAddress());
    }

}
