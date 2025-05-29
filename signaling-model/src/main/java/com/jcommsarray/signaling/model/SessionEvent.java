package com.jcommsarray.signaling.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public abstract class SessionEvent extends SignalingEvent {

    private String sessionId;

    private String userId;

    public SessionEvent(EventType type, String sessionId, String userId) {
        super(type);
        this.sessionId = sessionId;
        this.userId = userId;
    }

}
