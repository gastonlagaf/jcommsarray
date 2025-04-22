package com.gastonlagaf.udp.client.signaling;

import com.gastonlagaf.signaling.model.AddressCandidate;
import com.gastonlagaf.signaling.model.ClosingEvent;
import com.gastonlagaf.signaling.model.InviteEvent;

import java.util.List;

public interface SignalingEventHandler {

    List<AddressCandidate> handleInvite(InviteEvent event);

    void handleClose(ClosingEvent event);

}
