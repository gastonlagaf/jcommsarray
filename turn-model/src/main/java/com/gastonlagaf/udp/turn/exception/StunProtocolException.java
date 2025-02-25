package com.gastonlagaf.udp.client.stun.exception;

import lombok.Getter;

@Getter
public class StunProtocolException extends RuntimeException {

    private final Integer code;

    public StunProtocolException(String message, Integer code) {
        super(message);
        this.code = code;
    }

}
