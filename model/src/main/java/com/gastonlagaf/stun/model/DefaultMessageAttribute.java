package com.gastonlagaf.stun.model;

import lombok.Getter;

@Getter
public class DefaultMessageAttribute extends SingleValuedAttribute<byte[]> {

    public DefaultMessageAttribute(Integer type, Integer length, byte[] value) {
        super(type, length, value);
    }

}
