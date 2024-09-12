package com.gastonlagaf.stun.model;

import lombok.Getter;

@Getter
public class AddressFamilyAttribute extends SingleValuedAttribute<IpFamily> {

    public AddressFamilyAttribute(Integer type, Integer length, IpFamily value) {
        super(type, length, value);
    }

}
