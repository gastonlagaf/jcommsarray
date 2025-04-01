package com.gastonlagaf.signaling.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SessionClosedEvent extends SessionEvent {

    public SessionClosedEvent(String sessionId, String userId) {
        super(EventType.SESSION_CLOSED, sessionId, userId);
    }

}
