package com.jcommsarray.turn.model;

import lombok.Getter;

@Getter
public class AddressFamilyAttribute extends SingleValuedAttribute<IpFamily> {

    public AddressFamilyAttribute(Integer type, Integer length, IpFamily value) {
        super(type, length, value);
    }

}
