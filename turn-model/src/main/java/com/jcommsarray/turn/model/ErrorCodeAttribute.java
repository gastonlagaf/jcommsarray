package com.jcommsarray.turn.model;

import com.jcommsarray.turn.exception.StunProtocolException;
import lombok.Getter;

@Getter
public class ErrorCodeAttribute extends MessageAttribute {

    private final Integer code;

    private final String reasonPhrase;

    public ErrorCodeAttribute(Integer length, Integer code, String reasonPhrase) {
        super(KnownAttributeName.ERROR_CODE.getCode(), length);
        this.code = code;
        this.reasonPhrase = reasonPhrase;
    }

    public ErrorCodeAttribute(StunProtocolException exception) {
        super(KnownAttributeName.ERROR_CODE.getCode(), 0);
        this.code = exception.getCode();
        this.reasonPhrase = exception.getMessage();
    }

}
