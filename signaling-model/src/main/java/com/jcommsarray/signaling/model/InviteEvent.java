package com.jcommsarray.signaling.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class InviteEvent extends SessionEvent {

    private List<AddressCandidate> addresses;

    public InviteEvent(String sessionId, String userId, List<AddressCandidate> addresses) {
        super(EventType.INVITE, sessionId, userId);
        this.addresses = addresses;
    }

}
