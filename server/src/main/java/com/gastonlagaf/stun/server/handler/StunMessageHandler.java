package com.gastonlagaf.stun.server.handler;

import com.gastonlagaf.stun.server.model.ServerType;
import com.gastonlagaf.stun.server.model.StunRequest;
import com.gastonlagaf.stun.server.model.StunResponse;

import java.nio.channels.DatagramChannel;

public interface StunMessageHandler {

    StunResponse handle(ServerType serverType, DatagramChannel channel, StunRequest stunRequest);

}
