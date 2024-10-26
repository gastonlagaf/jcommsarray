package com.gastonlagaf.stun.server.model;

import com.gastonlagaf.stun.model.Message;
import com.gastonlagaf.stun.server.turn.TurnSession;
import lombok.Getter;

@Getter
public class ContexedMessage extends Message {

    private final TurnSession turnSession;

    public ContexedMessage(Message message, TurnSession turnSession) {
        super(message.getHeader(), message.getAttributes());
        this.turnSession = turnSession;
    }
}
