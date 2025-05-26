package com.jcommsarray.client.turn.proxy;

import com.jcommsarray.client.UdpClient;
import com.jcommsarray.client.turn.TurnClientProtocol;
import com.jcommsarray.client.turn.client.TurnClient;
import com.jcommsarray.test.protocol.ClientProtocol;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class TurnProxy<T> implements UdpClient<T> {

    private final ClientProtocol<T> targetProtocol;

    private final TurnClientProtocol<T> turnClientProtocol;

    private final Map<InetSocketAddress, Integer> channelBindings = new ConcurrentHashMap<>();

    private final TurnClient turnClient;

    public TurnProxy(ClientProtocol<T> targetProtocol, TurnClientProtocol<T> turnClientProtocol) {
        this.turnClientProtocol = turnClientProtocol;
        this.targetProtocol = targetProtocol;
        this.turnClient = (TurnClient) turnClientProtocol.getClient();
        this.turnClient.getChannelBindings().forEach((key, value) -> this.channelBindings.put(value, key));
    }

    @Override
    public CompletableFuture<Void> send(InetSocketAddress target, T message) {
        return Optional.ofNullable(channelBindings.get(target))
                .map(CompletableFuture::completedFuture)
                .orElseGet(() -> doCreateChannel(target))
                .thenCompose(it -> {
                    byte[] data = targetProtocol.serialize(message).array();
                    return turnClient.send(it, data);
                });

    }

    @Override
    public CompletableFuture<Void> send(InetSocketAddress source, InetSocketAddress target, T message) {
        return send(target, message);
    }

    @Override
    public CompletableFuture<T> sendAndReceive(InetSocketAddress target, T message) {
        return send(target, message).thenCompose(it -> targetProtocol.awaitResult(message));
    }

    @Override
    public CompletableFuture<T> sendAndReceive(InetSocketAddress source, InetSocketAddress target, T message) {
        return sendAndReceive(target, message);
    }

    @Override
    public void close() throws IOException {
        this.turnClientProtocol.close();
    }

    public InetSocketAddress getProxyAddress() {
        return turnClient.getProxyAddress();
    }

    private CompletableFuture<Integer> doCreateChannel(InetSocketAddress target) {
        return turnClient.createChannel(target).thenApply(it -> {
            channelBindings.put(target, it);
            return it;
        });
    }

}
