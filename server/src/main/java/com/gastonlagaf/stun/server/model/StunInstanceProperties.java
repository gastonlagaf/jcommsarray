package com.gastonlagaf.stun.server.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class StunInstanceProperties {

    private final String interfaceIp;

    private final Integer port;

    private final Integer alternatePort;

}
