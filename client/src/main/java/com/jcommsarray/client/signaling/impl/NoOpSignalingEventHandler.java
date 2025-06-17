package com.jcommsarray.client.signaling.impl;

import com.jcommsarray.client.ice.transfer.model.PeerConnectDetails;
import com.jcommsarray.client.signaling.SignalingEventHandler;
import com.jcommsarray.signaling.model.AddressCandidate;
import com.jcommsarray.signaling.model.ClosingEvent;
import com.jcommsarray.signaling.model.InviteEvent;
import com.jcommsarray.signaling.model.SignalingSubscriber;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;

public class NoOpSignalingEventHandler implements SignalingEventHandler {

    @Override
    public PeerConnectDetails handleInvite(InviteEvent event) {
        return PeerConnectDetails.INSTANCE;
    }

    @Override
    public void handleClose(ClosingEvent event) {
        // No-op
    }

}
