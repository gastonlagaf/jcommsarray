package com.gastonlagaf.udp.client.stun.model;

import lombok.Getter;

@Getter
public class ChannelNumberAttribute extends SingleValuedAttribute<Integer> {

    public static final Integer MIN_CHANNEL_NUMBER = 16384;

    public static final Integer MAX_CHANNEL_NUMBER = 20479;

    public ChannelNumberAttribute(Integer value) {
        super(KnownAttributeName.CHANNEL_NUMBER.getCode(), 0, value);
    }

    public ChannelNumberAttribute(Integer type, Integer length, Integer value) {
        super(type, length, value);
    }

}
