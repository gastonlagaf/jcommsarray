package com.gastonlagaf.turn.client;

import com.gastonlagaf.stun.model.Message;
import com.gastonlagaf.udp.client.UdpClient;

import java.io.Closeable;
import java.net.InetSocketAddress;
import java.util.List;

public interface TurnClient extends UdpClient<Message>, Closeable {

    void createPermission(List<InetSocketAddress> targets);

    Integer createChannel(InetSocketAddress target);

    Integer createChannel(Integer number, InetSocketAddress target);

    InetSocketAddress resolveChannel(Integer number);

    void send(Integer channelNumber, byte[] data);

    void send(InetSocketAddress receiver, byte[] data);

}
