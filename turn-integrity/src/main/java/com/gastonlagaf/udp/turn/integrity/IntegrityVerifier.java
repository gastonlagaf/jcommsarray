package com.gastonlagaf.udp.client.stun.integrity;

import com.gastonlagaf.udp.client.stun.model.Message;

public interface IntegrityVerifier {

    void check(Message message);

}
