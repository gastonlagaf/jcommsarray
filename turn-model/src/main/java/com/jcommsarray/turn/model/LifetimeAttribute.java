package com.jcommsarray.turn.model;

import lombok.Getter;

@Getter
public class LifetimeAttribute extends SingleValuedAttribute<Integer> {

    public static final Integer DELETE_ALLOCATION_LIFETIME_MARK = 0;

    public LifetimeAttribute(Integer type, Integer length, Integer value) {
        super(type, length, value);
    }

    public LifetimeAttribute(Integer value) {
        super(KnownAttributeName.LIFETIME.getCode(), Integer.BYTES, value);
    }

}
