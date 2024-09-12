package com.gastonlagaf.stun.model;

import lombok.Getter;

@Getter
public class ChannelNumberAttribute extends SingleValuedAttribute<Integer> {

    public ChannelNumberAttribute(Integer type, Integer length, Integer value) {
        super(type, length, value);
    }

}
