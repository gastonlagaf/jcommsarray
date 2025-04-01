package com.gastonlagaf.signaling.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StillAliveEvent extends SessionEvent {

    public StillAliveEvent(String sessionId, String userId) {
        super(EventType.STILL_ALIVE, sessionId, userId);
    }

}
