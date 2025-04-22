package com.gastonlagaf.udp.turn.codec.attribute.impl;

import com.gastonlagaf.udp.turn.codec.attribute.BaseMessageAttributeCodec;
import com.gastonlagaf.udp.turn.codec.util.CodecUtils;
import com.gastonlagaf.udp.turn.model.IntegerAttribute;
import com.gastonlagaf.udp.turn.model.LongAttribute;
import com.gastonlagaf.udp.turn.model.MessageHeader;

import java.nio.ByteBuffer;

public class LongAttributeCodec extends BaseMessageAttributeCodec<LongAttribute> {

    private static final Integer VALUE_LENGTH = 8;

    @Override
    protected Class<LongAttribute> getType() {
        return LongAttribute.class;
    }

    @Override
    protected LongAttribute decodeValue(MessageHeader messageHeader, ByteBuffer byteBuffer, Integer type, Integer length) {
        Long value = byteBuffer.getLong();
        return new LongAttribute(type, VALUE_LENGTH, value);
    }

    @Override
    protected byte[] encodeValue(MessageHeader messageHeader, LongAttribute messageAttribute) {
        return CodecUtils.longToByteArray(messageAttribute.getValue());
    }

}
