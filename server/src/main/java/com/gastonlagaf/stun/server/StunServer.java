package com.gastonlagaf.stun.server;

import com.gastonlagaf.stun.integrity.IntegrityVerifier;
import com.gastonlagaf.stun.server.handler.MessageHandler;
import com.gastonlagaf.stun.server.model.ContexedMessage;
import com.gastonlagaf.stun.server.model.ServerDispatcher;
import com.gastonlagaf.stun.server.model.StunServerProperties;
import com.gastonlagaf.stun.server.turn.TurnSessions;
import com.gastonlagaf.udp.UdpChannelRegistry;
import com.gastonlagaf.udp.UdpSockets;

import java.io.IOException;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StunServer {

    private final StunServerProperties properties;

    private final UdpSockets<ContexedMessage> udpSockets;

    public StunServer(StunServerProperties properties) {
        this(properties, null);
    }

    public StunServer(StunServerProperties properties, IntegrityVerifier verifier) {
        this.properties = properties;

        UdpChannelRegistry channelRegistry = new UdpChannelRegistry(properties.getWorkersCount());
        Map<String, TurnSessions> turnSessionsMap = properties.getEnableTurn()
                ? properties.getIpAddresses().stream().collect(Collectors.toMap(Function.identity(), it -> new TurnSessions()))
                : null;
        ServerDispatcher serverDispatcher = new ServerDispatcher(properties.getServers());
        MessageHandler messageHandler = new MessageHandler(serverDispatcher, turnSessionsMap, verifier, channelRegistry);

        this.udpSockets = new UdpSockets<>(messageHandler, channelRegistry);
    }

    public void start() {
        this.properties.getServers().values().forEach(it -> this.udpSockets.getRegistry().register(it.getHostName(), it.getPort()));
        this.udpSockets.start();
    }

    public void stop() {
        try {
            this.udpSockets.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
