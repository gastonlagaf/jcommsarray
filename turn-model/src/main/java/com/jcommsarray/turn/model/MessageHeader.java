package com.jcommsarray.turn.model;

import lombok.Getter;

import java.security.SecureRandom;

@Getter
public class MessageHeader {

    public static final Integer MAGIC_COOKIE = 0x2112A442;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public static final Integer LENGTH = 20;

    public static final Integer TRANSACTION_ID_LENGTH = 12;

    private final MessageType type;

    private final Integer length;

    private final Integer magicCookie;

    private final byte[] transactionId;

    public MessageHeader(Integer typeCode, Integer length, byte[] transactionId) {
        this.type = MessageType.ofCode(typeCode);
        this.length = length;
        this.magicCookie = MAGIC_COOKIE;
        this.transactionId = transactionId;
    }

    public MessageHeader(MessageHeader header, Integer length) {
        this(header.type.getCode(), length, header.getTransactionId());
    }

    public MessageHeader(MessageType type) {
        this(
                type.getCode(),
                0,
                generateTransactionId()
        );
    }

    public static byte[] generateTransactionId() {
        byte[] result = new byte[TRANSACTION_ID_LENGTH];
        SECURE_RANDOM.nextBytes(result);
        return result;
    }

}
