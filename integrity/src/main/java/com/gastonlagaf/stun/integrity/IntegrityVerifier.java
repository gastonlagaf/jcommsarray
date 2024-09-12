package com.gastonlagaf.stun.integrity;

import com.gastonlagaf.stun.model.Message;

public interface IntegrityVerifier {

    void check(Message message);

}
