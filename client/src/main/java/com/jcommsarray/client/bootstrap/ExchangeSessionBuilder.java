package com.jcommsarray.client.bootstrap;

import com.jcommsarray.client.model.StunProperties;
import com.jcommsarray.test.protocol.ClientProtocol;
import com.jcommsarray.test.socket.UdpSockets;
import com.jcommsarray.turn.integrity.user.model.UserDetails;
import lombok.RequiredArgsConstructor;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Optional;

@RequiredArgsConstructor
public class ExchangeSessionBuilder<T extends ClientProtocol<?>> {

    private static final Integer MIN_PORT = 1024;

    private static final Integer MAX_PORT = 65535;

    final UdpSockets sockets;

    String hostId;

    StunProperties stunConfig;

    StunProperties turnConfig;

    Duration socketTimeout;

    String realm;

    Integer minPort;

    Integer maxPort;

    public ExchangeSessionBuilder<T> withHostId(String hostId) {
        this.hostId = hostId;
        return this;
    }

    public ExchangeSessionBuilder<T> useStun(InetSocketAddress stunAddress) {
        this.stunConfig = new StunProperties(stunAddress, null);
        return this;
    }

    public ExchangeSessionBuilder<T> useStun(InetSocketAddress stunAddress, String username, String password, String realm) {
        UserDetails userDetails = new UserDetails(username, password, realm);
        this.stunConfig = new StunProperties(stunAddress, userDetails);
        return this;
    }

    public ExchangeSessionBuilder<T> useTurn(InetSocketAddress turnAddress) {
        this.turnConfig = new StunProperties(turnAddress, null);
        return this;
    }

    public ExchangeSessionBuilder<T> useTurn(InetSocketAddress turnAddress, String username, String password, String realm) {
        UserDetails userDetails = new UserDetails(username, password, realm);
        this.turnConfig = new StunProperties(turnAddress, userDetails);
        return this;
    }

    public ExchangeSessionBuilder<T> useSocketTimeout(Duration socketTimeout) {
        this.socketTimeout = socketTimeout;
        return this;
    }

    public ExchangeSessionBuilder<T> withPortRange(int minPort, int maxPort) {
        if (MIN_PORT > this.minPort || MAX_PORT < maxPort) {
            throw new IllegalArgumentException("Port range must be between " + MIN_PORT + " and " + MAX_PORT);
        }
        if (minPort > maxPort) {
            throw new IllegalArgumentException("Start port must be less than the end port");
        }
        this.minPort = minPort;
        this.maxPort = maxPort;
        return this;
    }

    public ExchangeSessionBuilder<T> withRealm(String realm) {
        this.realm = realm;
        return this;
    }

    public ExchangeSession<T> build() {
        this.minPort = Optional.ofNullable(this.minPort).orElse(MIN_PORT);
        this.maxPort = Optional.ofNullable(this.maxPort).orElse(MAX_PORT);
        this.realm = Optional.ofNullable(this.realm).orElse("default");

        return new ExchangeSession<>(sockets, hostId, stunConfig, turnConfig, socketTimeout, realm, minPort, maxPort);
    }

}
