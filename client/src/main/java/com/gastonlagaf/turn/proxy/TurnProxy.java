package com.gastonlagaf.turn.proxy;

import com.gastonlagaf.stun.client.model.StunClientProperties;
import com.gastonlagaf.stun.model.Message;
import com.gastonlagaf.turn.TurnClientProtocol;
import com.gastonlagaf.turn.client.TurnClient;
import com.gastonlagaf.turn.client.impl.UdpTurnClient;
import com.gastonlagaf.udp.client.UdpClient;
import com.gastonlagaf.udp.protocol.ClientProtocol;
import com.gastonlagaf.udp.socket.UdpSockets;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class TurnProxy<T> implements UdpClient<T> {

    private final TurnClient turnClient;

    private final ClientProtocol<T> targetProtocol;

    public TurnProxy(StunClientProperties properties, UdpSockets<Message> sockets, TurnClientProtocol<T> turnClientProtocol, Map<Integer, InetSocketAddress> channelBindings, ClientProtocol<T> targetProtocol) {
        this.turnClient = new UdpTurnClient(properties, sockets, turnClientProtocol, channelBindings);
        this.targetProtocol = targetProtocol;
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

    @Override
    public void close() throws IOException {
        this.turnClient.close();
    }

}
