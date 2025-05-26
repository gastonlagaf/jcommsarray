package com.jcommsarray.signaling.impl;

import com.jcommsarray.signaling.model.AddressCandidate;
import com.jcommsarray.signaling.model.ClosingEvent;
import com.jcommsarray.signaling.model.InviteEvent;
import com.jcommsarray.signaling.SignalingEventHandler;

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
