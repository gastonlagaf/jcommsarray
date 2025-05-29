package com.jcommsarray.signaling.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class InviteAnsweredEvent extends SessionEvent {

    private List<AddressCandidate> addresses;

    public InviteAnsweredEvent(String sessionId, String userId, List<AddressCandidate> addresses) {
        super(EventType.INVITE_ANSWERED, sessionId, userId);
        this.addresses = addresses;
    }

}
