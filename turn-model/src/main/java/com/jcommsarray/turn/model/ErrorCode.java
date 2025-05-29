package com.jcommsarray.turn.model;

import com.jcommsarray.turn.util.CodeMappingUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    TRY_ALTERNATE(300),
    BAD_REQUEST(400),
    UNAUTHENTICATED(401),
    FORBIDDEN(403),
    UNKNOWN_ATTRIBUTE(420),
    ALLOCATION_MISMATCH(437),
    STALE_NONCE(438),
    ADDRESS_FAMILY_NOT_SUPPORTED(440),
    WRONG_CREDENTIALS(441),
    UNSUPPORTED_TRANSPORT_PROTOCOL(442),
    PEER_ADDRESS_FAMILY_MISMATCH(443),
    ALLOCATION_QUOTA_REACHED(486),
    SERVER_ERROR(500),
    INSUFFICIENT_CAPACITY(508),
    ROLE_CONFLICT(487);

    private final Integer code;

    private static final Map<Integer, ErrorCode> codeMap = CodeMappingUtils.mapValues(ErrorCode.values(), ErrorCode::getCode);

    public static ErrorCode ofCode(Integer code) {
        return codeMap.get(code);
    }

}
