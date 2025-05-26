package com.jcommsarray.turn.model;

public class LongAttribute extends SingleValuedAttribute<Long> {

    public LongAttribute(Integer type, Long value) {
        super(type, Long.BYTES, value);
    }

    public LongAttribute(Integer type, Integer length, Long value) {
        super(type, length, value);
    }

}
