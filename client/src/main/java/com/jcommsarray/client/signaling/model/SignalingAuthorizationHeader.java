package com.jcommsarray.client.signaling.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SignalingAuthorizationHeader {

    private final String name;

    private final String value;

}
