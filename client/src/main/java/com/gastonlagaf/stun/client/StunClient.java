package com.gastonlagaf.stun.client;

import com.gastonlagaf.stun.model.Message;
import com.gastonlagaf.stun.model.NatBehaviour;

import java.io.Closeable;

public interface StunClient extends Closeable {

    Message get();

    NatBehaviour checkMappingBehaviour();

    NatBehaviour checkFilteringBehaviour();

}
