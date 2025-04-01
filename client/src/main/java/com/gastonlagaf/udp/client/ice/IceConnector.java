package com.gastonlagaf.udp.client.ice;

import com.gastonlagaf.udp.client.ice.model.Candidate;

import java.net.InetSocketAddress;

public interface IceConnector {

    Candidate connect(String peerIdentifier);

}
