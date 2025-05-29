package com.jcommsarray.client.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.Properties;
import java.util.Random;

@Getter
@RequiredArgsConstructor
public class ClientProperties {

    private final InetSocketAddress hostAddress;

    private final InetSocketAddress targetAddress;

    private final InetSocketAddress stunAddress;

    private final InetSocketAddress turnAddress;

    private final Long socketTimeout;

    public ClientProperties(InetAddress hostIpAddress, Properties properties) {
        String hostIp = hostIpAddress.getHostAddress();
        Integer hostPort = Optional.ofNullable(properties.getProperty("host-port"))
                .map(Integer::parseInt)
                .orElseGet(() -> new Random().nextInt(40000, Short.MAX_VALUE << 1));
        this.hostAddress = new InetSocketAddress(hostIp, hostPort);

        this.targetAddress = mapAddress("target", properties);
        this.stunAddress = mapAddress("stun", properties);
        this.turnAddress = mapAddress("turn", properties);


        this.socketTimeout = Optional.ofNullable(properties.getProperty("socket-timeout"))
                .map(Long::parseLong)
                .orElse(5000L);
    }

    private InetSocketAddress mapAddress(String prefix, Properties properties) {
        Optional<InetSocketAddress> resultOptional = Optional.ofNullable(properties.getProperty(prefix + "-port"))
                .map(Integer::parseInt)
                .flatMap(
                        it -> Optional.ofNullable(properties.getProperty(prefix + "-host"))
                                .map(ij -> new InetSocketAddress(ij, it))
                );
        return !"turn".equals(prefix) ?
                resultOptional.orElseThrow(() -> new IllegalStateException("Some " + prefix + " properties are missing"))
                : resultOptional.orElse(null);
    }

}
