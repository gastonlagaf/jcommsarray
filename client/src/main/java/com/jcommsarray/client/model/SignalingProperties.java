package com.jcommsarray.client.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.time.Duration;

@Getter
@RequiredArgsConstructor
public class SignalingProperties {

    private final URI uri;

    private final Duration receiveTimeout;

}
