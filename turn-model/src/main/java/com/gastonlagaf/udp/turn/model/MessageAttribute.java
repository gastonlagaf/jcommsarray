package com.gastonlagaf.udp.turn.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MessageAttribute {

    private final Integer type;

    private final Integer length;

}
