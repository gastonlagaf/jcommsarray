package com.gastonlagaf.udp;

import com.gastonlagaf.udp.client.UdpClient;
import com.gastonlagaf.udp.codec.CommunicationCodec;
import lombok.RequiredArgsConstructor;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public abstract class BaseUdpClient<T> implements UdpClient<T> {

    protected final UdpSockets<T> udpSockets;

    protected final CommunicationCodec<T> communicationCodec;

    @Override
    public void send(InetSocketAddress source, InetSocketAddress target, T message) {
        byte[] data = communicationCodec.encode(message).array();
        udpSockets.send(source, target, data);
    }

    @Override
    public T sendAndReceive(InetSocketAddress source, InetSocketAddress target, T message) {
        byte[] data = communicationCodec.encode(message).array();

        CompletableFuture<T> responseFuture = registerAwait(message);
        udpSockets.send(source, target, data);

        return responseFuture.join();
    }

}
