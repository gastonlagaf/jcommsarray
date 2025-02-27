package com.gastonlagaf.udp.client.turn.proxy;

import com.gastonlagaf.udp.client.UdpClient;
import com.gastonlagaf.udp.client.model.ClientProperties;
import com.gastonlagaf.udp.client.turn.TurnClientProtocol;
import com.gastonlagaf.udp.client.turn.client.TurnClient;
import com.gastonlagaf.udp.protocol.ClientProtocol;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

public class TurnProxy<T> implements UdpClient<T> {

    private final TurnClient turnClient;

    private final ClientProtocol<T> targetProtocol;

    private final TurnClientProtocol<T> turnClientProtocol;

    public TurnProxy(ClientProperties properties, ClientProtocol<T> targetProtocol) {
        this.turnClientProtocol = new TurnClientProtocol<>(targetProtocol, properties);
        this.targetProtocol = targetProtocol;
        this.turnClient = (TurnClient) turnClientProtocol.getClient();
    }

    @Override
    public void send(InetSocketAddress target, T message) {
        byte[] data = targetProtocol.serialize(message).array();
        turnClient.send(target, data);
    }

    @Override
    public void send(InetSocketAddress source, InetSocketAddress target, T message) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CompletableFuture<T> sendAndReceive(InetSocketAddress target, T message) {
        CompletableFuture<T> result = targetProtocol.awaitResult(message);
        send(target, message);
        return result;
    }

    @Override
    public CompletableFuture<T> sendAndReceive(InetSocketAddress source, InetSocketAddress target, T message) {
        throw new UnsupportedOperationException();
    }

    public void start(InetSocketAddress... addresses) {
        this.turnClientProtocol.start(addresses);
    }

    @Override
    public void close() throws IOException {
        this.turnClientProtocol.close();
    }

}
