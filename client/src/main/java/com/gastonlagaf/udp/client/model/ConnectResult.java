package com.gastonlagaf.udp.client.model;

import com.gastonlagaf.udp.protocol.ClientProtocol;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.net.InetSocketAddress;

@Getter
@RequiredArgsConstructor
public class ConnectResult<T extends ClientProtocol<?>> {

    private final InetSocketAddress opponentAddress;

    private final T protocol;

}
