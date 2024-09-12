package com.gastonlagaf.stun.model;

import com.gastonlagaf.stun.util.CodeMappingUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum MessageType {

    BINDING_REQUEST(0x0001),
    BINDING_INDICATION(0x0010),
    BINDING_SUCCESS_RESPONSE(0x0101),
    BINDING_FAILURE_RESPONSE(0x0111);

    private final Integer code;

    public static final Integer BINDING_MASK = 0x0110;

    private static final Map<Integer, MessageType> codeMap = CodeMappingUtils.mapValues(
            MessageType.values(), MessageType::getCode
    );

    public static MessageType ofCode(Integer code) {
        return Optional.ofNullable(codeMap.get(code))
                .orElseThrow(() -> new IllegalArgumentException("Unknown message type " + code));
    }

}
