package com.jcommsarray.client.ice.model;

import com.jcommsarray.client.ice.protocol.IceProtocol;
import com.jcommsarray.turn.model.Protocol;
import lombok.Getter;

import java.net.InetSocketAddress;
import java.util.Optional;

@Getter
public class Candidate implements Comparable<Candidate> {

    private static final Integer MAX_COMPONENT_ID = 256;

    private final InetSocketAddress hostAddress;

    private final InetSocketAddress actualAddress;

    private final CandidateType type;

    private final Integer priority;

    private final String foundation;

    private final IceProtocol iceProtocol;

    public Candidate(InetSocketAddress hostAddress, InetSocketAddress actualAddress, CandidateType type, Integer localPreference, Integer componentId, IceProtocol iceProtocol) {
        this.hostAddress = hostAddress;
        this.actualAddress = Optional.ofNullable(actualAddress).orElse(hostAddress);
        this.type = type;
        this.iceProtocol = iceProtocol;
        this.priority = calculatePriority(this.type, localPreference, componentId);
        this.foundation = getFoundation(this.type, this.actualAddress);
    }

    public Candidate(InetSocketAddress actualAddress, CandidateType type, Integer priority) {
        this.hostAddress = actualAddress;
        this.actualAddress = Optional.ofNullable(actualAddress).orElse(hostAddress);
        this.type = type;
        this.priority = priority;
        this.iceProtocol = null;
        this.foundation = getFoundation(this.type, this.actualAddress);
    }

    private Integer calculatePriority(CandidateType type, Integer localPreference, Integer componentId) {
        return 2 ^ 24 * type.getPreference()
                + 2 ^ 8 * localPreference
                + MAX_COMPONENT_ID - componentId;
    }

    private String getFoundation(CandidateType type, InetSocketAddress address) {
        return Protocol.UDP.name() + type.name() + address.getAddress().getHostName();
    }

    @Override
    public int compareTo(Candidate o) {
        return o.priority.compareTo(this.priority);
    }

}
