package com.gastonlagaf.signaling.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class InviteEvent extends SessionEvent {

    public InviteEvent(EventType type, String sessionId, String userId) {
        super(EventType.INVITE, sessionId, userId);
    }

}
