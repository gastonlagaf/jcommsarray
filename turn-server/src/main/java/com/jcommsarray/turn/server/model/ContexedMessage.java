package com.jcommsarray.turn.server.model;

import com.jcommsarray.turn.model.Message;
import com.jcommsarray.turn.server.turn.TurnSession;
import lombok.Getter;

@Getter
public class ContexedMessage extends Message {

    private final TurnSession turnSession;

    public ContexedMessage(Message message, TurnSession turnSession) {
        super(message.getHeader(), message.getAttributes());
        this.turnSession = turnSession;
    }
}
