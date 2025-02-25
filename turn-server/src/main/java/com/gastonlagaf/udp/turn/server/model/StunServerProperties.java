package com.gastonlagaf.udp.client.stun.server.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
public class StunServerProperties {

    private final StunInstanceProperties primaryInstance;

    private final StunInstanceProperties secondaryInstance;

    private final Boolean enableTurn;

    private final Integer workersCount;

    public Map<ServerType, InetSocketAddress> getServers() {
        Map<ServerType, InetSocketAddress> result = new HashMap<>();
        result.put(ServerType.PRIMARY, new InetSocketAddress(primaryInstance.getInterfaceIp(), primaryInstance.getPort()));
        Optional.ofNullable(primaryInstance.getAlternatePort())
                .ifPresent(it -> result.put(ServerType.PRIMARY_ALTERNATIVE, new InetSocketAddress(primaryInstance.getInterfaceIp(), it)));
        Optional.ofNullable(secondaryInstance)
                .ifPresent(it -> result.put(ServerType.SECONDARY, new InetSocketAddress(it.getInterfaceIp(), it.getPort())));
        Optional.ofNullable(secondaryInstance)
                .flatMap(it -> Optional.ofNullable(it.getAlternatePort()))
                .ifPresent(it -> result.put(ServerType.SECONDARY, new InetSocketAddress(secondaryInstance.getInterfaceIp(), it)));
        return result;
    }

    public List<String> getIpAddresses() {
        return Stream.of(primaryInstance, secondaryInstance)
                .filter(Objects::nonNull)
                .map(StunInstanceProperties::getInterfaceIp)
                .toList();
    }

}
