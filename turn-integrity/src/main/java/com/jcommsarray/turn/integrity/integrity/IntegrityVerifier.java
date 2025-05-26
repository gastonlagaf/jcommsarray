package com.jcommsarray.turn.integrity.integrity;

import com.jcommsarray.turn.model.Message;

public interface IntegrityVerifier {

    void check(Message message);

}
