package com.gastonlagaf.stun.model;

import lombok.Getter;

@Getter
public class RequestedTransportAttribute extends SingleValuedAttribute<Protocol> {

    public RequestedTransportAttribute(Integer type, Integer length, Protocol value) {
        super(type, length, value);
    }

}
