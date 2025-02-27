package com.gastonlagaf.udp.turn.model;

import com.gastonlagaf.udp.turn.util.CodeMappingUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum IpFamily {

    IPV4(0x01, 4),
    IPV6(0x02, 16);

    private final Integer code;

    private final Integer addressLength;

    private static final Map<Integer, IpFamily> codeMap = CodeMappingUtils.mapValues(
            IpFamily.values(), IpFamily::getCode
    );

    public static IpFamily ofCode(Integer code) {
        return codeMap.get(code);
    }

}
