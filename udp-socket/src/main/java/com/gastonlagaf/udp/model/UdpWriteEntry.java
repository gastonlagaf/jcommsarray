package com.gastonlagaf.udp.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.net.InetSocketAddress;

@Getter
@RequiredArgsConstructor
public class UdpWriteEntry {

    private final InetSocketAddress target;

    private final byte[] data;

}
