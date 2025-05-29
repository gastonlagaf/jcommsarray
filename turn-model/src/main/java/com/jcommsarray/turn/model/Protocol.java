package com.jcommsarray.turn.model;

import com.jcommsarray.turn.util.CodeMappingUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum Protocol {

    TCP(6),
    UDP(17);

    private final Integer code;

    private static final Map<Integer, Protocol> codeMap = CodeMappingUtils.mapValues(Protocol.values(), Protocol::getCode);

    public static Protocol ofCode(Integer code) {
        return codeMap.get(code);
    }

}
