package com.jcommsarray.client.stun.client;

import com.jcommsarray.client.UdpClient;
import com.jcommsarray.turn.model.Message;
import com.jcommsarray.turn.model.NatBehaviour;

import java.io.Closeable;
import java.net.InetSocketAddress;

public interface StunClient extends UdpClient<Message>, Closeable {

    InetSocketAddress getReflexiveAddress();

    NatBehaviour checkMappingBehaviour();

    NatBehaviour checkFilteringBehaviour();

}
