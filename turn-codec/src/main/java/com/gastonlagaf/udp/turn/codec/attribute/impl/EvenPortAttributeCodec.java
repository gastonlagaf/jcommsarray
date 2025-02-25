package com.gastonlagaf.udp.client.stun.codec.attribute.impl;

import com.gastonlagaf.udp.client.stun.codec.attribute.BaseMessageAttributeCodec;
import com.gastonlagaf.udp.client.stun.model.EvenPortAttribute;
import com.gastonlagaf.udp.client.stun.model.MessageHeader;

import java.nio.ByteBuffer;

public class EvenPortAttributeCodec extends BaseMessageAttributeCodec<EvenPortAttribute> {

    private static final Integer VALUE_SIZE = 1;

    private static final Byte FALSE_ATTRIBUTE_VALUE = 0;

    private static final Byte TRUE_ATTRIBUTE_VALUE = (byte) 128;

    @Override
    protected Class<EvenPortAttribute> getType() {
        return EvenPortAttribute.class;
    }

    @Override
    protected EvenPortAttribute decodeValue(MessageHeader messageHeader, ByteBuffer byteBuffer, Integer type, Integer length) {
        byte value = byteBuffer.get();
        return new EvenPortAttribute(type, VALUE_SIZE, FALSE_ATTRIBUTE_VALUE == value);
    }

    @Override
    protected byte[] encodeValue(MessageHeader messageHeader, EvenPortAttribute messageAttribute) {
        return messageAttribute.getValue() ? new byte[]{TRUE_ATTRIBUTE_VALUE} : new byte[]{FALSE_ATTRIBUTE_VALUE};
    }

}
