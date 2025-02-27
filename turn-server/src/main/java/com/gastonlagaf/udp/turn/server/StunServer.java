package com.gastonlagaf.udp.turn.server;

import com.gastonlagaf.udp.socket.UdpChannelRegistry;
import com.gastonlagaf.udp.socket.UdpSockets;
import com.gastonlagaf.udp.turn.integrity.IntegrityVerifier;
import com.gastonlagaf.udp.turn.server.model.ContexedMessage;
import com.gastonlagaf.udp.turn.server.model.ServerDispatcher;
import com.gastonlagaf.udp.turn.server.model.StunServerProperties;
import com.gastonlagaf.udp.turn.server.protocol.StunTurnProtocol;
import com.gastonlagaf.udp.turn.server.turn.TurnSessions;

import java.io.IOException;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StunServer {

    private final StunServerProperties properties;

    private final UdpSockets<ContexedMessage> udpSockets;

    private final IntegrityVerifier integrityVerifier;

    public StunServer(StunServerProperties properties) {
        this(properties, null);
    }

    public StunServer(StunServerProperties properties, IntegrityVerifier verifier) {
        this.properties = properties;

        UdpChannelRegistry channelRegistry = new UdpChannelRegistry(properties.getWorkersCount());
        this.udpSockets = new UdpSockets<>(channelRegistry);

        this.integrityVerifier = verifier;
    }

    public void start() {
        Map<String, TurnSessions> turnSessionsMap = properties.getEnableTurn()
                ? properties.getIpAddresses().stream().collect(Collectors.toMap(Function.identity(), it -> new TurnSessions()))
                : null;
        ServerDispatcher serverDispatcher = new ServerDispatcher(properties.getServers());
        StunTurnProtocol stunTurnProtocol = new StunTurnProtocol(
                serverDispatcher, turnSessionsMap, integrityVerifier, properties.getWorkersCount()
        );

        this.properties.getServers().values().forEach(it -> this.udpSockets.getRegistry().register(it));
        this.udpSockets.start(stunTurnProtocol);
    }

    public void stop() {
        try {
            this.udpSockets.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
