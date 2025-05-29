package com.jcommsarray.turn.codec.attribute.impl;

import com.jcommsarray.turn.codec.attribute.BaseMessageAttributeCodec;
import com.jcommsarray.turn.codec.util.CodecUtils;
import com.jcommsarray.turn.model.IntegerAttribute;
import com.jcommsarray.turn.model.MessageHeader;

import java.nio.ByteBuffer;

public class IntegerAttributeCodec extends BaseMessageAttributeCodec<IntegerAttribute> {

    private static final Integer VALUE_LENGTH = 4;

    @Override
    protected Class<IntegerAttribute> getType() {
        return IntegerAttribute.class;
    }

    @Override
    protected IntegerAttribute decodeValue(MessageHeader messageHeader, ByteBuffer byteBuffer, Integer type, Integer length) {
        Integer value = byteBuffer.getInt();
        return new IntegerAttribute(type, VALUE_LENGTH, value);
    }

    @Override
    protected byte[] encodeValue(MessageHeader messageHeader, IntegerAttribute messageAttribute) {
        return CodecUtils.intToByteArray(messageAttribute.getValue());
    }

}
