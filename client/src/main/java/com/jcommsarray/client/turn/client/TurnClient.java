package com.jcommsarray.client.turn.client;

import com.jcommsarray.client.UdpClient;
import com.jcommsarray.turn.model.Message;

import java.io.Closeable;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface TurnClient extends UdpClient<Message>, Closeable {

    CompletableFuture<Void> createPermission(List<InetSocketAddress> targets);

    CompletableFuture<Integer> createChannel(InetSocketAddress target);

    CompletableFuture<Integer> createChannel(Integer number, InetSocketAddress target);

    CompletableFuture<Void> send(Integer channelNumber, byte[] data);

    CompletableFuture<Void> send(InetSocketAddress receiver, byte[] data);

    InetSocketAddress start(InetSocketAddress hostAddress);

    InetSocketAddress getProxyAddress();

    Map<Integer, InetSocketAddress> getChannelBindings();

}
