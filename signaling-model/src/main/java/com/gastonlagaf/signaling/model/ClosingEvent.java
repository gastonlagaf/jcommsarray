package com.gastonlagaf.signaling.model;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ClosingEvent extends SessionEvent {

    public ClosingEvent(String sessionId, String userId) {
        super(EventType.CLOSING, sessionId, userId);
    }

}
