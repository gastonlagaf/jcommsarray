package com.gastonlagaf.udp.client.signaling;

import com.gastonlagaf.signaling.model.ClosingEvent;
import com.gastonlagaf.signaling.model.InviteEvent;

public interface SignalingEventHandler {

    Boolean handleInvite(InviteEvent event);

    void handleClose(ClosingEvent event);

}
