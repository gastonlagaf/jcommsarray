package com.gastonlagaf.udp.turn.model;

public class IntegerAttribute extends SingleValuedAttribute<Integer> {

    public IntegerAttribute(Integer type, Integer value) {
        this(type, Integer.BYTES, value);
    }

    public IntegerAttribute(Integer type, Integer length, Integer value) {
        super(type, length, value);
    }

}
