package com.gastonlagaf.signaling.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SessionCreatedEvent extends SessionEvent {

    public SessionCreatedEvent(String sessionId, String userId) {
        super(EventType.SESSION_CREATED, sessionId, userId);
    }

}
