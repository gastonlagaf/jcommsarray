package com.gastonlagaf.udp.turn.model;

import lombok.Getter;

@Getter
public class PasswordAlgorithmAttribute extends MessageAttribute {

    public static final Integer DEFAULT_VALUE_LENGTH = 4;

    private final PasswordAlgorithm value;

    public PasswordAlgorithmAttribute(Integer type, Integer length, PasswordAlgorithm value) {
        super(type, length);
        this.value = value;
    }

    public PasswordAlgorithmAttribute(PasswordAlgorithm value) {
        super(KnownAttributeName.PASSWORD_ALGORITHM.getCode(), DEFAULT_VALUE_LENGTH);
        this.value = value;
    }

}
