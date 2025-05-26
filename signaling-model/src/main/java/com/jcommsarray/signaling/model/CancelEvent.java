package com.jcommsarray.signaling.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CancelEvent extends SessionEvent {

    private String reason;

    public CancelEvent(String sessionId, String userId, String reason) {
        super(EventType.CANCEL, sessionId, userId);
        this.reason = reason;
    }

}
