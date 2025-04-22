package com.gastonlagaf.udp.client.ice.model;

import com.gastonlagaf.udp.client.ice.protocol.IceProtocol;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.net.InetSocketAddress;

@Getter
@RequiredArgsConstructor
public class IceConnectResult {

    private final InetSocketAddress opponentAddress;

    private final IceProtocol iceProtocol;

}
