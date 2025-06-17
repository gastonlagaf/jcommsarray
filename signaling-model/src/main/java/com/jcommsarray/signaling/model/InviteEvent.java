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

    private String password;

    public InviteEvent(String sessionId, String userId, List<AddressCandidate> addresses, String password) {
        super(EventType.INVITE, sessionId, userId);
        this.addresses = addresses;
        this.password = password;
    }

}
