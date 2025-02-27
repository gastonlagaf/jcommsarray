package com.gastonlagaf.udp.client.stun.client;

import com.gastonlagaf.udp.client.UdpClient;
import com.gastonlagaf.udp.turn.model.Message;
import com.gastonlagaf.udp.turn.model.NatBehaviour;

import java.io.Closeable;
import java.net.InetSocketAddress;

public interface StunClient extends UdpClient<Message>, Closeable {

    InetSocketAddress getReflexiveAddress();

    NatBehaviour checkMappingBehaviour();

    NatBehaviour checkFilteringBehaviour();

}
