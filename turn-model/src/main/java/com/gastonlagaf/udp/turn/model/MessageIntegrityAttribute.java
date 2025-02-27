package com.gastonlagaf.udp.turn.model;

import lombok.Getter;

@Getter
public class MessageIntegrityAttribute extends MessageAttribute {

    private final Boolean isSha256;

    private final byte[] value;

    private final byte[] precedingBytes;

    private final String username;

    private final String realm;

    private final String password;

    private final PasswordAlgorithm passwordAlgorithm;

    public MessageIntegrityAttribute(Integer type, byte[] precedingBytes, String username, String realm, String password, PasswordAlgorithm passwordAlgorithm) {
        this(type, null, precedingBytes, username, realm, password, passwordAlgorithm);
    }

    public MessageIntegrityAttribute(Integer type, byte[] value, byte[] precedingBytes) {
        this(type, value, precedingBytes, null, null, null, null);
    }

    public MessageIntegrityAttribute(Integer type, byte[] value, byte[] precedingBytes, String username, String realm, String password, PasswordAlgorithm passwordAlgorithm) {
        super(type, null != value ? value.length : 0);
        this.isSha256 = !KnownAttributeName.MESSAGE_INTEGRITY.getCode().equals(type);
        this.value = value;
        this.precedingBytes = precedingBytes;
        this.username = username;
        this.realm = realm;
        this.password = password;
        this.passwordAlgorithm = passwordAlgorithm;
    }

}
