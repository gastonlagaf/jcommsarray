package com.gastonlagaf.stun.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public class Message {

    private final MessageHeader header;

    private final Map<Integer, MessageAttribute> attributes;

    public Message() {
        this(
                new MessageHeader(MessageType.BINDING_REQUEST),
                Map.of()
        );
    }

    public Message(Map<Integer, MessageAttribute> attributes) {
        this(
                new MessageHeader(MessageType.BINDING_REQUEST),
                Optional.ofNullable(attributes).orElse(Map.of())
        );
    }

}
