package com.gastonlagaf.udp.client.stun.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MessageAttribute {

    private final Integer type;

    private final Integer length;

}
