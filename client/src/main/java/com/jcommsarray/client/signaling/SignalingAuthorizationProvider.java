package com.jcommsarray.client.signaling;

import com.jcommsarray.client.signaling.model.SignalingAuthorizationHeader;

public interface SignalingAuthorizationProvider {

    SignalingAuthorizationHeader getAuthorization();

}
