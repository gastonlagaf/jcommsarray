package com.gastonlagaf.stun.codec.attribute.impl;

import com.gastonlagaf.stun.codec.attribute.BaseMessageAttributeCodec;
import com.gastonlagaf.stun.model.DefaultMessageAttribute;
import com.gastonlagaf.stun.model.MessageHeader;

import java.nio.ByteBuffer;

public class DefaultAttributeCodec extends BaseMessageAttributeCodec<DefaultMessageAttribute> {

    @Override
    protected Class<DefaultMessageAttribute> getType() {
        return DefaultMessageAttribute.class;
    }

    @Override
    protected byte[] encodeValue(MessageHeader messageHeader, DefaultMessageAttribute messageAttribute) {
        return messageAttribute.getValue();
    }

    @Override
    protected DefaultMessageAttribute decodeValue(MessageHeader messageHeader, ByteBuffer byteBuffer, Integer type, Integer length) {
        byte[] value = new byte[length];
        byteBuffer.get(value);
        return new DefaultMessageAttribute(type, length, value);
    }

}