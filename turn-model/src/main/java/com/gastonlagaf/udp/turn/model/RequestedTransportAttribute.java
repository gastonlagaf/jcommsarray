package com.gastonlagaf.udp.turn.model;

import lombok.Getter;

@Getter
public class RequestedTransportAttribute extends SingleValuedAttribute<Protocol> {

    public RequestedTransportAttribute(Integer type, Integer length, Protocol value) {
        super(type, length, value);
    }

    public RequestedTransportAttribute(Protocol value) {
        this(KnownAttributeName.REQUESTED_TRANSPORT.getCode(), 0, value);
    }

}
