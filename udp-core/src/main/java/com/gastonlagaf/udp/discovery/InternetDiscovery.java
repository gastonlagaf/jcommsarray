package com.gastonlagaf.udp.discovery;

import lombok.SneakyThrows;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;

public class InternetDiscovery {

    @SneakyThrows
    public static InetAddress getAddress() {
        return NetworkInterface.networkInterfaces()
                .filter(it -> {
                    try {
                        return !it.isLoopback() && it.isUp() && it.getDisplayName().startsWith("e");
                    } catch (SocketException e) {
                        throw new RuntimeException(e);
                    }
                })
                .flatMap(it -> Collections.list(it.getInetAddresses()).stream())
                .filter(it -> it instanceof Inet4Address)
                .findFirst()
                .orElse(null);
    }

}
