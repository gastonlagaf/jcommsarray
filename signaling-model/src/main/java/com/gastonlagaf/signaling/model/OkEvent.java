package com.gastonlagaf.signaling.model;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class OkEvent extends SessionEvent {

    public OkEvent(String sessionId, String userId) {
        super(EventType.OK, sessionId, userId);
    }

}
