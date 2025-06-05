package com.jcommsarray.client.signaling.impl;

import com.jcommsarray.client.signaling.SignalingAuthorizationProvider;
import com.jcommsarray.client.signaling.model.SignalingAuthorizationHeader;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultSignalingAuthorizationProvider implements SignalingAuthorizationProvider {

    private static final String NAME_HEADER = "name";

    private final String name;

    @Override
    public SignalingAuthorizationHeader getAuthorization() {
        return new SignalingAuthorizationHeader(NAME_HEADER, name);
    }

}
