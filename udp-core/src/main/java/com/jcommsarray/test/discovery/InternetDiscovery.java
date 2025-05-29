package com.jcommsarray.test.discovery;

import lombok.SneakyThrows;

import java.io.UncheckedIOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class InternetDiscovery {

    public static InetAddress getAddress() {
        return getAddressesStream()
                .findFirst()
                .orElse(null);
    }

    public static List<InetAddress> getAddresses() {
        return getAddressesStream().toList();
    }

    @SneakyThrows
    private static Stream<InetAddress> getAddressesStream() {
        return NetworkInterface.networkInterfaces()
                .filter(it -> {
                    try {
                        return it.isUp() && !it.isLoopback();
                    } catch (SocketException e) {
                        throw new UncheckedIOException(e);
                    }
                })
                .flatMap(it -> Collections.list(it.getInetAddresses()).stream())
                .filter(it -> it instanceof Inet4Address && !it.isLinkLocalAddress());
    }

}
