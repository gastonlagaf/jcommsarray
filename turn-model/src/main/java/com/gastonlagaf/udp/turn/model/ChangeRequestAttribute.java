package com.gastonlagaf.udp.turn.model;

import lombok.Getter;

@Getter
public class ChangeRequestAttribute extends MessageAttribute {

    private static final Integer CHANGE_IP_FLAG_VALUE = 4;

    private static final Integer CHANGE_PORT_FLAG_VALUE = 2;

    private static final Integer CHANGE_IP_FLAG_POSITION = 2;

    private static final Integer CHANGE_PORT_FLAG_POSITION = 1;

    private final Boolean changeHost;

    private final Boolean changePort;

    public ChangeRequestAttribute(Boolean changeHost, Boolean changePort) {
        super(KnownAttributeName.CHANGE_REQUEST.getCode(), 0);
        this.changeHost = changeHost;
        this.changePort = changePort;
    }

    public ChangeRequestAttribute(byte[] value) {
        super(KnownAttributeName.CHANGE_REQUEST.getCode(), value.length);
        byte encodedValue = value[value.length - 1];
        this.changeHost = CHANGE_IP_FLAG_VALUE == (encodedValue & (1 << CHANGE_IP_FLAG_POSITION));
        this.changePort = CHANGE_PORT_FLAG_VALUE == (encodedValue & (1 << CHANGE_PORT_FLAG_POSITION));
    }

    public byte[] getEncodedValue() {
        int result = 0;
        if (changeHost) {
            result |= 1 << CHANGE_IP_FLAG_POSITION;
        }
        if (changePort) {
            result |= 1 << CHANGE_PORT_FLAG_POSITION;
        }
        return new byte[] {
                (byte) (result >> 24),
                (byte) (result >> 16),
                (byte) (result >> 8),
                (byte) (result)
        };
    }

}
