package com.jcommsarray.client.model;

import com.jcommsarray.test.protocol.ClientProtocol;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.net.InetSocketAddress;

@Getter
@RequiredArgsConstructor
public class ConnectResult<T extends ClientProtocol<?>> {

    private final InetSocketAddress opponentAddress;

    private final T protocol;

}
