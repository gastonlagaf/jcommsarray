package com.gastonlagaf.stun.model;

import lombok.Getter;

@Getter
public class LifetimeAttribute extends SingleValuedAttribute<Integer> {

    public LifetimeAttribute(Integer type, Integer length, Integer value) {
        super(type, length, value);
    }

}
