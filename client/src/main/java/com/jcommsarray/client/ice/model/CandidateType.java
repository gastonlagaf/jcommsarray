package com.jcommsarray.client.ice.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CandidateType {

    HOST(126),
    PEER_REFLEXIVE(110),
    SERVER_REFLEXIVE(0);

    @Getter
    private final Integer preference;

}
