package com.gastonlagaf.udp.client.stun.model;

import lombok.Getter;

@Getter
public class AddressErrorCodeAttribute extends MessageAttribute {

    private final IpFamily ipFamily;

    private final Integer code;

    private final String reasonPhrase;

    public AddressErrorCodeAttribute(Integer type, Integer length, IpFamily ipFamily, Integer code, String reasonPhrase) {
        super(type, length);
        this.ipFamily = ipFamily;
        this.code = code;
        this.reasonPhrase = reasonPhrase;
    }

}
