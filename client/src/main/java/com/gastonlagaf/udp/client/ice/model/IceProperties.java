package com.gastonlagaf.udp.client.ice.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Duration;

@Getter
public class IceProperties {

    private final String sourceContactId;

    private final String targetContactId;

    private final IceRole role;

    private final Duration ta;

    private final Integer componentId;

    private final Integer minPort;

    private final Integer maxPort;

    public IceProperties(IceRole role, Integer componentId, Integer minPort, Integer maxPort) {
        this(role, Duration.ofMillis(50L), componentId, minPort, maxPort);
    }

    public IceProperties(IceRole role, Duration ta, Integer componentId, Integer minPort, Integer maxPort) {
        this.role = role;
        this.ta = ta;
        this.componentId = componentId;

        if (maxPort < minPort) {
            throw new IllegalArgumentException("maxPort must be greater than minPort");
        }
        this.minPort = minPort;
        this.maxPort = maxPort;
    }
}
