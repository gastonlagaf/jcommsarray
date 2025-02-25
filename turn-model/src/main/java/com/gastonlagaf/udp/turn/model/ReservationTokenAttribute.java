package com.gastonlagaf.udp.client.stun.model;

import lombok.Getter;

@Getter
public class ReservationTokenAttribute extends SingleValuedAttribute<Long> {

    public ReservationTokenAttribute(Integer type, Integer length, Long value) {
        super(type, length, value);
    }

}
