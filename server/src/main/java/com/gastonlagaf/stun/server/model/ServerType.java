package com.gastonlagaf.stun.server.model;

import lombok.RequiredArgsConstructor;

import java.net.InetSocketAddress;
import java.util.Map;

@RequiredArgsConstructor
public enum ServerType {

    PRIMARY,
    PRIMARY_ALTERNATIVE,
    SECONDARY,
    SECONDARY_ALTERNATIVE;

    private static final Map<ServerType, ServerType> OPPOSITE_TYPES_MAP = Map.of(
            PRIMARY, SECONDARY,
            SECONDARY, PRIMARY,
            PRIMARY_ALTERNATIVE, SECONDARY_ALTERNATIVE,
            SECONDARY_ALTERNATIVE, PRIMARY_ALTERNATIVE
    );

    private static final Map<ServerType, ServerType> ALTERNATIVE_TYPES_MAP = Map.of(
            PRIMARY, PRIMARY_ALTERNATIVE,
            PRIMARY_ALTERNATIVE, PRIMARY,
            SECONDARY, SECONDARY_ALTERNATIVE,
            SECONDARY_ALTERNATIVE, SECONDARY
    );

    public static ServerType change(ServerType currentServerType, Boolean changeHost, Boolean changePort) {
        ServerType result = currentServerType;
        if (changeHost) {
            result = OPPOSITE_TYPES_MAP.get(result);
        }
        if (changePort) {
            result = ALTERNATIVE_TYPES_MAP.get(result);
        }
        return result;
    }

    public static ServerType getOther(ServerType serverType) {
        return OPPOSITE_TYPES_MAP.get(serverType);
    }

    public InetSocketAddress getSocketAddress(StunServerProperties properties) {
        return switch (this) {
            case PRIMARY -> new InetSocketAddress(
                    properties.getPrimaryInstance().getInterfaceIp(), properties.getPrimaryInstance().getPort()
            );
            case PRIMARY_ALTERNATIVE -> new InetSocketAddress(
                    properties.getPrimaryInstance().getInterfaceIp(), properties.getPrimaryInstance().getAlternatePort()
            );
            case SECONDARY -> new InetSocketAddress(
                    properties.getSecondaryInstance().getInterfaceIp(), properties.getSecondaryInstance().getPort()
            );
            case SECONDARY_ALTERNATIVE -> new InetSocketAddress(
                    properties.getSecondaryInstance().getInterfaceIp(), properties.getSecondaryInstance().getAlternatePort()
            );
        };
    }

}
