package com.gastonlagaf.stun.model;

import com.gastonlagaf.stun.util.CodeMappingUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum KnownAttributeName {
    
    MAPPED_ADDRESS(0x0001),
    USERNAME(0x0006),
    MESSAGE_INTEGRITY(0x0008),
    MESSAGE_INTEGRITY_SHA256(0x001C),
    ERROR_CODE(0x0009),
    UNKNOWN_ATTRIBUTES(0x000A),
    ACCESS_TOKEN(0x001B),
    PASSWORD_ALGORITHM(0x001D),
    PASSWORD_ALGORITHMS(0x8002),
    REALM(0x0014),
    XOR_MAPPED_ADDRESS(0x0020),
    CHANGE_REQUEST(0x0003),
    RESPONSE_ORIGIN(0x802B),
    OTHER_ADDRESS(0x802C),
    RESPONSE_PORT(0x0027),
    PADDING(0x0026),
    CHANNEL_NUMBER(0x000C),
    LIFETIME(0x000D),
    XOR_PEER_ADDRESS(0x0012),
    DATA(0x0013),
    XOR_RELAYED_ADDRESS(0x0016),
    REQUESTED_ADDRESS_FAMILY(0x0017),
    EVEN_PORT(0x0018),
    REQUESTED_TRANSPORT(0x0019),
    DONT_FRAGMENT(0x001A),
    RESERVATION_TOKEN(0x0022),
    ADDITIONAL_ADDRESS_FAMILY(0x8000),
    ADDRESS_ERROR_CODE(0x8001),
    ICMP(0x8004);

    private final Integer code;

    private static final Map<Integer, KnownAttributeName> codeMap = CodeMappingUtils.mapValues(
            KnownAttributeName.values(), KnownAttributeName::getCode
    );

    public static KnownAttributeName ofCode(Integer code) {
        return codeMap.get(code);
    }
    
}
