package com.jcommsarray.client.ice.model;

import lombok.Getter;

@Getter
public class IceProperties {

    private final String sourceContactId;

    private final String targetContactId;

    private final String realm;

    private final IceRole role;

    private final Integer componentId;

    private final Integer retries;

    private final Integer minPort;

    private final Integer maxPort;

    public IceProperties(IceRole role, Integer componentId, Integer retries, Integer minPort, Integer maxPort) {
        this(null, null, null, role, componentId, retries, minPort, maxPort);
    }

    public IceProperties(String sourceContactId, String targetContactId, String realm, IceRole role, Integer componentId, Integer retries, Integer minPort, Integer maxPort) {
        this.sourceContactId = sourceContactId;
        this.targetContactId = targetContactId;
        this.realm = realm;
        this.role = role;
        this.componentId = componentId;
        this.retries = retries;

        if (maxPort < minPort) {
            throw new IllegalArgumentException("maxPort must be greater than minPort");
        }
        this.minPort = minPort;
        this.maxPort = maxPort;
    }
}
