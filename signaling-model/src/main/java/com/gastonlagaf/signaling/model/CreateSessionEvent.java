package com.gastonlagaf.signaling.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateSessionEvent extends SessionEvent {

    public CreateSessionEvent(String subscriberId) {
        super(EventType.CREATE_SESSION, null, subscriberId);
    }

}
