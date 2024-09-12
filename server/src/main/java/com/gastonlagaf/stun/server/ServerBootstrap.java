package com.gastonlagaf.stun.server;

import com.gastonlagaf.stun.server.impl.UdpStunServer;
import com.gastonlagaf.stun.server.model.StunInstanceProperties;
import com.gastonlagaf.stun.server.model.StunServerProperties;

public class ServerBootstrap {

    public static void main(String[] args) {
        StunInstanceProperties instanceProperties = new StunInstanceProperties("127.0.0.1", 3478, 3479);
        StunServerProperties stunServerProperties = new StunServerProperties(instanceProperties, null);
        StunServer server = new UdpStunServer(stunServerProperties);
        server.start();
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
    }

}
