package com.jcommsarray.client.bootstrap;

import com.jcommsarray.test.protocol.ClientProtocol;
import com.jcommsarray.test.socket.UdpSockets;
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

    InetSocketAddress stunAddress;

    InetSocketAddress turnAddress;

    Duration socketTimeout;

    Integer minPort;

    Integer maxPort;

    public ExchangeSessionBuilder<T> withHostId(String hostId) {
        this.hostId = hostId;
        return this;
    }

    public ExchangeSessionBuilder<T> useStun(InetSocketAddress stunAddress) {
        this.stunAddress = stunAddress;
        return this;
    }

    public ExchangeSessionBuilder<T> useTurn(InetSocketAddress turnAddress) {
        this.turnAddress = turnAddress;
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

    public ExchangeSession<T> build() {
        this.minPort = Optional.ofNullable(this.minPort).orElse(MIN_PORT);
        this.maxPort = Optional.ofNullable(this.maxPort).orElse(MAX_PORT);

        return new ExchangeSession<>(sockets, hostId, stunAddress, turnAddress, socketTimeout, minPort, maxPort);
    }

}
