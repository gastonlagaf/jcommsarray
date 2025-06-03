package com.jcommsarray.client.signaling;

import com.jcommsarray.signaling.model.AddressCandidate;
import com.jcommsarray.signaling.model.ClosingEvent;
import com.jcommsarray.signaling.model.InviteEvent;

import java.util.List;

public interface SignalingEventHandler {

    List<AddressCandidate> handleInvite(InviteEvent event);

    void handleClose(ClosingEvent event);

}
