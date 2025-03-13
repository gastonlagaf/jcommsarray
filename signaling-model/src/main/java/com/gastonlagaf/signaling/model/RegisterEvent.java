package com.gastonlagaf.signaling.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class RegisterEvent extends SignalingEvent {

    private List<AddressCandidate> candidates;

    public RegisterEvent(List<AddressCandidate> candidates) {
        super(EventType.REGISTER);
        this.candidates = candidates;
    }

    public RegisterEvent(SignalingSubscriber signalingSubscriber) {
        super(EventType.REGISTER);
        this.candidates = signalingSubscriber.getAddresses();
    }

}
