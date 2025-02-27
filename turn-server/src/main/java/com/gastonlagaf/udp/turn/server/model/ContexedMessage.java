package com.gastonlagaf.udp.turn.server.model;

import com.gastonlagaf.udp.turn.model.Message;
import com.gastonlagaf.udp.turn.server.turn.TurnSession;
import lombok.Getter;

@Getter
public class ContexedMessage extends Message {

    private final TurnSession turnSession;

    public ContexedMessage(Message message, TurnSession turnSession) {
        super(message.getHeader(), message.getAttributes());
        this.turnSession = turnSession;
    }
}
