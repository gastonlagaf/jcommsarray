package com.gastonlagaf.udp.client.signaling.impl;

import com.gastonlagaf.signaling.model.ClosingEvent;
import com.gastonlagaf.signaling.model.InviteEvent;
import com.gastonlagaf.udp.client.signaling.SignalingEventHandler;

public class NoOpSignalingEventHandler implements SignalingEventHandler {

    @Override
    public Boolean handleInvite(InviteEvent event) {
        return true;
    }

    @Override
    public void handleClose(ClosingEvent event) {
        // No-op
    }

}
