package com.jcommsarray.turn.model;

import com.jcommsarray.turn.exception.StunProtocolException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public class Message {

    private final MessageHeader header;

    private final MessageAttributes attributes;

    public Message(MessageType type, byte[] txId, StunProtocolException ex) {
        this(
                new MessageHeader(type.getCode(), 0, txId),
                Map.of(
                        KnownAttributeName.ERROR_CODE.getCode(), new ErrorCodeAttribute(ex)
                )
        );
    }

    public Message(Map<Integer, MessageAttribute> attributes) {
        this(new MessageHeader(MessageType.BINDING_REQUEST), attributes);
    }

    public Message(MessageHeader header, Map<Integer, MessageAttribute> attributes) {
        this(
                header,
                Optional.ofNullable(attributes)
                        .map(MessageAttributes::singleOnly)
                        .orElse(MessageAttributes.EMPTY)
        );
    }

}
