package com.gastonlagaf.udp.client.stun.model;

import lombok.Getter;

@Getter
public class EvenPortAttribute extends SingleValuedAttribute<Boolean> {

    public EvenPortAttribute(Integer type, Integer length, Boolean value) {
        super(type, length, value);
    }

}
