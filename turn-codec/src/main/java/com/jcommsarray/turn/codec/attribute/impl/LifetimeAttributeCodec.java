package com.jcommsarray.turn.codec.attribute.impl;

import com.jcommsarray.turn.codec.attribute.BaseMessageAttributeCodec;
import com.jcommsarray.turn.codec.util.CodecUtils;
import com.jcommsarray.turn.model.LifetimeAttribute;
import com.jcommsarray.turn.model.MessageHeader;

import java.nio.ByteBuffer;

public class LifetimeAttributeCodec extends BaseMessageAttributeCodec<LifetimeAttribute> {

    private static final Integer VALUE_LENGTH = 4;

    @Override
    protected Class<LifetimeAttribute> getType() {
        return LifetimeAttribute.class;
    }

    @Override
    protected LifetimeAttribute decodeValue(MessageHeader messageHeader, ByteBuffer byteBuffer, Integer type, Integer length) {
        Integer value = byteBuffer.getInt();
        return new LifetimeAttribute(type, VALUE_LENGTH, value);
    }

    @Override
    protected byte[] encodeValue(MessageHeader messageHeader, LifetimeAttribute messageAttribute) {
        return CodecUtils.intToByteArray(messageAttribute.getValue());
    }

}
