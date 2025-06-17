package com.jcommsarray.client.signaling;

import com.jcommsarray.client.ice.transfer.model.PeerConnectDetails;
import com.jcommsarray.signaling.model.AddressCandidate;
import com.jcommsarray.signaling.model.ClosingEvent;
import com.jcommsarray.signaling.model.InviteEvent;
import com.jcommsarray.signaling.model.SignalingSubscriber;

import java.util.List;

public interface SignalingEventHandler {

    PeerConnectDetails handleInvite(InviteEvent event);

    void handleClose(ClosingEvent event);

}
