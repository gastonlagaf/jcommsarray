package com.gastonlagaf.udp.client.turn.client;

import com.gastonlagaf.udp.client.UdpClient;
import com.gastonlagaf.udp.turn.model.Message;

import java.io.Closeable;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface TurnClient extends UdpClient<Message>, Closeable {

    void createPermission(List<InetSocketAddress> targets);

    Integer createChannel(InetSocketAddress target);

    Integer createChannel(Integer number, InetSocketAddress target);

    CompletableFuture<Void> send(Integer channelNumber, byte[] data);

    CompletableFuture<Void> send(InetSocketAddress receiver, byte[] data);

    InetSocketAddress start(InetSocketAddress hostAddress);

    InetSocketAddress getProxyAddress();

}
