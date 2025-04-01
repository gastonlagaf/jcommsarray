package com.gastonlagaf.signaling.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ClosedEvent extends SessionEvent {

    public ClosedEvent(String sessionId, String userId) {
        super(EventType.CLOSED, sessionId, userId);
    }

}
