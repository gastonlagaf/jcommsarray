package com.jcommsarray.turn.model;

import com.jcommsarray.turn.util.CodeMappingUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum PasswordAlgorithm {

    MD5(0x0001, "MD5"),
    SHA256(0x0002, "SHA-256"),;

    private final Integer code;

    private final String digestName;

    private static final Map<Integer, PasswordAlgorithm> codeMap = CodeMappingUtils.mapValues(
            PasswordAlgorithm.values(), PasswordAlgorithm::getCode
    );

    public static PasswordAlgorithm ofCode(Integer code) {
        return codeMap.get(code);
    }

}
