package com.gastonlagaf.udp.turn.codec.attribute.impl;

import com.gastonlagaf.udp.turn.codec.attribute.BaseMessageAttributeCodec;
import com.gastonlagaf.udp.turn.codec.util.CodecUtils;
import com.gastonlagaf.udp.turn.model.IntegerAttribute;
import com.gastonlagaf.udp.turn.model.LifetimeAttribute;
import com.gastonlagaf.udp.turn.model.MessageHeader;

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
