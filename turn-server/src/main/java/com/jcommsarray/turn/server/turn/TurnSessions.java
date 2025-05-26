package com.jcommsarray.turn.server.turn;

import com.jcommsarray.test.socket.UdpSockets;
import com.jcommsarray.turn.exception.StunProtocolException;
import com.jcommsarray.turn.model.ErrorCode;
import com.jcommsarray.turn.model.Protocol;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalListener;
import com.github.benmanes.caffeine.cache.Scheduler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class TurnSessions {

    private static final Integer MIN_ALLOCATABLE_PORT = 49152;

    private static final Integer MAX_ALLOCATABLE_PORT = 65535;

    public static final Long DEFAULT_SESSION_DURATION_MINUTES = 10L;

    private static final String COLON = ":";

    private final UdpSockets udpSockets;

    private final Integer startPort;

    private final Integer endPort;

    private final Cache<String, TurnSession> sessions;

    private final Map<InetSocketAddress, TurnSession> sessionsByServer;

    private final TreeSet<Integer> allocatedPorts = new TreeSet<>();

    private final ReentrantLock lock = new ReentrantLock();

    public TurnSessions(UdpSockets udpSockets) {
        this(udpSockets, MIN_ALLOCATABLE_PORT, MAX_ALLOCATABLE_PORT);
    }

    public TurnSessions(UdpSockets udpSockets, Integer startPort, Integer endPort) {
        if (startPort < MIN_ALLOCATABLE_PORT || startPort > MAX_ALLOCATABLE_PORT) {
            throw new ExceptionInInitializerError("Start port is out of range (" + MIN_ALLOCATABLE_PORT + " - " + MAX_ALLOCATABLE_PORT + ")");
        }
        if (endPort < MIN_ALLOCATABLE_PORT || endPort > MAX_ALLOCATABLE_PORT) {
            throw new ExceptionInInitializerError("End port is out of range (" + MIN_ALLOCATABLE_PORT + " - " + MAX_ALLOCATABLE_PORT + ")");
        }
        if (endPort < startPort) {
            throw new ExceptionInInitializerError("Start port is greater than end port");
        }
        this.udpSockets = udpSockets;
        this.startPort = startPort;
        this.endPort = endPort;
        this.sessions = Caffeine.newBuilder()
                .scheduler(Scheduler.systemScheduler())
                .expireAfterWrite(Duration.ofMinutes(DEFAULT_SESSION_DURATION_MINUTES))
                .removalListener((RemovalListener<String, TurnSession>) (key, value, cause) -> {
                    if (cause.wasEvicted()) {
                        closeChannel(value);
                    }
                })
                .build();
        this.sessionsByServer = new HashMap<>();
    }

    public TurnSession get(InetSocketAddress sourceAddress, InetSocketAddress serverAddress, Protocol protocol) {
        String key = getKey(sourceAddress, serverAddress, protocol);
        return sessions.getIfPresent(key);
    }

    public TurnSession get(InetSocketAddress serverAddress) {
        return sessionsByServer.get(serverAddress);
    }

    public void put(TurnSession turnSession, Protocol protocol) {
        String key = getKey(turnSession.getClientAddress(), turnSession.getServerAddress(), protocol);

        sessions.put(key, turnSession);
        sessionsByServer.put(turnSession.getServerAddress(), turnSession);
        allocatedPorts.add(turnSession.getServerAddress().getPort());
    }

    public void remove(TurnSession turnSession) {
        String key = getKey(turnSession.getClientAddress(), turnSession.getServerAddress(), Protocol.UDP);

        sessions.invalidate(key);
        sessionsByServer.remove(turnSession.getServerAddress());
        allocatedPorts.remove(turnSession.getServerAddress().getPort());

        log.info("Turn server socket {} has been closed", turnSession.getServerAddress().toString());
    }

    public Integer allocatePort() {
        try {
            lock.lock();
            return doAllocate();
        } finally {
            lock.unlock();
        }
    }

    private Integer doAllocate() {
        if (allocatedPorts.isEmpty()) {
            return startPort;
        }
        Integer edgePort = allocatedPorts.last();
        if (edgePort < endPort) {
            return edgePort + 1;
        }
        for (int i = startPort; i <= endPort; i++) {
            if (allocatedPorts.contains(i)) {
                continue;
            }
            return i;
        }
        throw new StunProtocolException("No ports available for allocation", ErrorCode.INSUFFICIENT_CAPACITY.getCode());
    }

    private String getKey(InetSocketAddress sourceAddress, InetSocketAddress targetAddress, Protocol protocol) {
        return sourceAddress.getHostName() + COLON + targetAddress.getPort() + COLON
                + targetAddress.getHostName() + COLON + targetAddress.getPort() + COLON
                + protocol;
    }

    private void closeChannel(TurnSession turnSession) {
        if (null == turnSession) {
            return;
        }
        allocatedPorts.remove(turnSession.getServerAddress().getPort());
        udpSockets.getRegistry().deregister(turnSession.getSelectionKey());
        log.info("Evicted turn server socket {}", turnSession.getServerAddress().toString());
    }

}
