package com.gastonlagaf.stun.client.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class StunClientProperties {

    private final String interfaceIp;

    private final Integer clientPort;

    private final String serverHost;

    private final Integer serverPort;

    private final Integer socketTimeout;

}
