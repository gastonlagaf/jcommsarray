package com.gastonlagaf.udp.client.ice.model;

import com.gastonlagaf.udp.client.ice.protocol.IceProtocol;
import com.gastonlagaf.udp.turn.model.Protocol;
import lombok.Getter;

import java.net.InetSocketAddress;

@Getter
public class Candidate {

    private static final Integer MAX_COMPONENT_ID = 256;

    private final InetSocketAddress address;

    private final CandidateType type;

    private final Integer priority;

    private final String foundation;

    private final IceProtocol protocol;

    public Candidate(InetSocketAddress address, CandidateType type, Integer localPreference, Integer componentId, IceProtocol protocol) {
        this.address = address;
        this.type = type;
        this.protocol = protocol;
        this.priority = calculatePriority(type, localPreference, componentId);
        this.foundation = getFoundation(type, address);
    }

    private Integer calculatePriority(CandidateType type, Integer localPreference, Integer componentId) {
        return 2 ^ 24 * type.getPreference()
                + 2 ^ 8 * localPreference
                + MAX_COMPONENT_ID - componentId;
    }

    private String getFoundation(CandidateType type, InetSocketAddress address) {
        return Protocol.UDP.name() + type.name() + address.getAddress().getHostName();
    }

}
