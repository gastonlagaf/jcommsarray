package com.gastonlagaf.udp.client.signaling.impl;

import com.gastonlagaf.signaling.model.AddressCandidate;
import com.gastonlagaf.signaling.model.ClosingEvent;
import com.gastonlagaf.signaling.model.InviteEvent;
import com.gastonlagaf.udp.client.signaling.SignalingEventHandler;

import java.util.List;

public class NoOpSignalingEventHandler implements SignalingEventHandler {

    @Override
    public List<AddressCandidate> handleInvite(InviteEvent event) {
        return List.of();
    }

    @Override
    public void handleClose(ClosingEvent event) {
        // No-op
    }

}
