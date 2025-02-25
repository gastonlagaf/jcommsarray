package com.gastonlagaf.udp.client.stun.server;

import com.gastonlagaf.udp.client.stun.server.model.StunInstanceProperties;
import com.gastonlagaf.udp.client.stun.server.model.StunServerProperties;

public class ServerBootstrap {

    public static void main(String[] args) {
        StunServerProperties stunServerProperties = new StunServerProperties(
                new StunInstanceProperties("127.0.0.1", 3478, 3479),
                null, true, 1
        );
        StunServer stunServer = new StunServer(stunServerProperties);
        stunServer.start();
    }

}
