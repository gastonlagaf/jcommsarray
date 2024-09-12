package com.gastonlagaf.stun.model;

import lombok.Getter;

@Getter
public abstract class SingleValuedAttribute<T> extends MessageAttribute {

    private final T value;

    public SingleValuedAttribute(Integer type, Integer length, T value) {
        super(type, length);
        this.value = value;
    }
}
