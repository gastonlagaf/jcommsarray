package com.jcommsarray.signaling.stomp.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Getter
@RequiredArgsConstructor
public class StompMessage<T> {

    private final StompMessageType type;

    private final Map<String, String> headers;

    private final T payload;

    public StompMessage(StompMessageType type, Map<String, String> headers) {
        this(type, headers, null);
    }
}
