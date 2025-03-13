package com.gastonlagaf.signaling.model;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CancelEvent extends SessionEvent {

    public CancelEvent(String sessionId, String userId) {
        super(EventType.CANCEL, sessionId, userId);
    }

}
