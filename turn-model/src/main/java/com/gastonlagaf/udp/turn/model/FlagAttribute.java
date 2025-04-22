package com.gastonlagaf.udp.turn.model;

public class FlagAttribute extends SingleValuedAttribute<Boolean> {

    public FlagAttribute(Integer type) {
        super(type, null, true);
    }

    public FlagAttribute(Integer type, Integer length, Boolean value) {
        super(type, length, value);
    }

}
