package com.gastonlagaf.udp.client.stun.codec.attribute.impl;

import com.gastonlagaf.udp.client.stun.codec.attribute.BaseMessageAttributeCodec;
import com.gastonlagaf.udp.client.stun.model.ErrorCodeAttribute;
import com.gastonlagaf.udp.client.stun.model.MessageHeader;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ErrorCodeAttributeCodec extends BaseMessageAttributeCodec<ErrorCodeAttribute> {

    private static final Integer CLASS_BYTES_LENGTH = 3;

    private static final Integer CLASS_AND_NUMBER_LENGTH = CLASS_BYTES_LENGTH + 1;

    private static final Integer LAST_ELEMENT_OFFSET = 1;

    private static final Integer ERROR_CODE_MULTIPLIER = 100;

    @Override
    protected Class<ErrorCodeAttribute> getType() {
        return ErrorCodeAttribute.class;
    }

    @Override
    protected ErrorCodeAttribute decodeValue(MessageHeader messageHeader, ByteBuffer byteBuffer, Integer type, Integer length) {
        byte[] classBytes = new byte[CLASS_BYTES_LENGTH];
        byteBuffer.get(classBytes);
        int errorClass = classBytes[CLASS_BYTES_LENGTH - LAST_ELEMENT_OFFSET];
        int errorNumber = byteBuffer.get();
        int errorCode = errorClass * ERROR_CODE_MULTIPLIER + errorNumber;

        byte[] reasonBytes = new byte[length - CLASS_AND_NUMBER_LENGTH];
        byteBuffer.get(reasonBytes);
        String reasonPhrase = new String(reasonBytes);

        return new ErrorCodeAttribute(length, errorCode, reasonPhrase);
    }

    @Override
    protected byte[] encodeValue(MessageHeader messageHeader, ErrorCodeAttribute messageAttribute) {
        int errorClass = messageAttribute.getCode() / ERROR_CODE_MULTIPLIER;
        int errorNumber = messageAttribute.getCode() % ERROR_CODE_MULTIPLIER;

        byte[] classBytes = new byte[CLASS_BYTES_LENGTH];
        classBytes[CLASS_BYTES_LENGTH - LAST_ELEMENT_OFFSET] = (byte) errorClass;

        byte[] reasonBytes = messageAttribute.getReasonPhrase().getBytes(StandardCharsets.UTF_8);

        ByteBuffer buffer = ByteBuffer.allocate(CLASS_AND_NUMBER_LENGTH + reasonBytes.length);
        buffer.put(classBytes);
        buffer.put((byte) errorNumber);
        buffer.put(reasonBytes);

        return buffer.array();
    }

}
