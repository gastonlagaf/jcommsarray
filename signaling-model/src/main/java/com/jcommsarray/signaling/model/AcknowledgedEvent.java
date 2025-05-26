package com.jcommsarray.signaling.model;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class AcknowledgedEvent extends SessionEvent {

    public AcknowledgedEvent(String sessionId, String userId) {
        super(EventType.ACKNOWLEDGED, sessionId, userId);
    }

}
