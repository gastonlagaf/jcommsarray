package com.gastonlagaf.handler;

import com.gastonlagaf.stun.model.Message;

import java.net.InetSocketAddress;

@FunctionalInterface
public interface MessageConsumer {

    void handle(InetSocketAddress receiverAddress, InetSocketAddress senderAddress, Message message);

}
