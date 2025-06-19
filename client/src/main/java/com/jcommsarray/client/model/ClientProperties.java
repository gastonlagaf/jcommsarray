package com.jcommsarray.client.model;

import com.jcommsarray.turn.integrity.user.model.UserDetails;
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

    private static final String TARGET_PREFIX = "target";
    private static final String STUN_PREFIX = "stun";
    private static final String TURN_PREFIX = "turn";

    private static final String HOST_PORT_PROPERTY = "host-port";
    private static final String SOCKET_TIMEOUT_PROPERTY = "socket-timeout";

    private static final String HOST_SUFFIX = "-host";
    private static final String PORT_SUFFIX = "-port";
    private static final String USER_SUFFIX = "-user";
    private static final String PASSWORD_SUFFIX = "-password";
    private static final String REALM_SUFFIX = "-realm";

    private static final Long DEFAULT_SOCKET_TIMEOUT = 5000L;

    private final InetSocketAddress hostAddress;

    private final InetSocketAddress targetAddress;

    private final StunProperties stunConfig;

    private final StunProperties turnConfig;

    private final Long socketTimeout;

    public ClientProperties(InetSocketAddress hostAddress, InetSocketAddress targetAddress, InetSocketAddress stunAddress, InetSocketAddress turnAddress, Long socketTimeout) {
        this.hostAddress = hostAddress;
        this.targetAddress = targetAddress;
        this.stunConfig = new StunProperties(stunAddress, null);
        this.turnConfig = new StunProperties(turnAddress, null);
        this.socketTimeout = socketTimeout;
    }

    public ClientProperties(InetAddress hostIpAddress, Properties properties) {
        String hostIp = hostIpAddress.getHostAddress();
        Integer hostPort = Optional.ofNullable(properties.getProperty(HOST_PORT_PROPERTY))
                .map(Integer::parseInt)
                .orElseGet(() -> new Random().nextInt(40000, Short.MAX_VALUE << 1));

        this.hostAddress = new InetSocketAddress(hostIp, hostPort);

        this.targetAddress = mapAddress(TARGET_PREFIX, properties);

        this.stunConfig = getConfig(STUN_PREFIX, properties);
        this.turnConfig = getConfig(TURN_PREFIX, properties);

        this.socketTimeout = Optional.ofNullable(properties.getProperty(SOCKET_TIMEOUT_PROPERTY))
                .map(Long::parseLong)
                .orElse(DEFAULT_SOCKET_TIMEOUT);
    }

    private StunProperties getConfig(String prefix, Properties properties) {
        InetSocketAddress address = mapAddress(prefix, properties);

        Optional<String> username = Optional.ofNullable(properties.getProperty(prefix + USER_SUFFIX));
        Optional<String> password = Optional.ofNullable(properties.getProperty(prefix + PASSWORD_SUFFIX));
        Optional<String> realm = Optional.ofNullable(properties.getProperty(prefix + REALM_SUFFIX));

        if (username.isPresent() && password.isPresent() && realm.isPresent()) {
            UserDetails userDetails = new UserDetails(username.get(), password.get(), realm.get());
            return new StunProperties(address, userDetails);
        }
        return new StunProperties(address, null);
    }

    private InetSocketAddress mapAddress(String prefix, Properties properties) {
        Optional<InetSocketAddress> resultOptional = Optional.ofNullable(properties.getProperty(prefix + PORT_SUFFIX))
                .map(Integer::parseInt)
                .flatMap(
                        it -> Optional.ofNullable(properties.getProperty(prefix + HOST_SUFFIX))
                                .map(ij -> new InetSocketAddress(ij, it))
                );
        return !TURN_PREFIX.equals(prefix) ?
                resultOptional.orElseThrow(() -> new IllegalStateException("Some " + prefix + " properties are missing"))
                : resultOptional.orElse(null);
    }

}
