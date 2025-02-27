package com.gastonlagaf.udp.turn.integrity;

import com.gastonlagaf.udp.turn.model.Message;

public interface IntegrityVerifier {

    void check(Message message);

}
