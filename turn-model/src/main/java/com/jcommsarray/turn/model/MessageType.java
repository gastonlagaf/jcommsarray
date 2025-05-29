package com.jcommsarray.turn.model;

import com.jcommsarray.turn.util.CodeMappingUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum MessageType {

    INBOUND_CHANNEL_DATA(-0x0002),
    OUTBOUND_CHANNEL_DATA(-0x0001),
    BINDING_REQUEST(0x0001),
    BINDING_RESPONSE(0x0101),
    ALLOCATE(0x003),
    REFRESH(0x004),
    SEND(0x006),
    DATA(0x007),
    CREATE_PERMISSION(0x008),
    CHANNEL_BIND(0x009);

    private final Integer code;

    private static final Map<Integer, MessageType> codeMap = CodeMappingUtils.mapValues(
            MessageType.values(), MessageType::getCode
    );

    public static MessageType ofCode(Integer code) {
        return Optional.ofNullable(codeMap.get(code))
                .orElseThrow(() -> new IllegalArgumentException("Unknown message type " + code));
    }

}
