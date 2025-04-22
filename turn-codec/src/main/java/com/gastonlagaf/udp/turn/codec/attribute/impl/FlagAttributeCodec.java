package com.gastonlagaf.udp.turn.codec.attribute.impl;

import com.gastonlagaf.udp.turn.codec.attribute.BaseMessageAttributeCodec;
import com.gastonlagaf.udp.turn.model.FlagAttribute;
import com.gastonlagaf.udp.turn.model.MessageHeader;

import java.nio.ByteBuffer;

public class FlagAttributeCodec extends BaseMessageAttributeCodec<FlagAttribute> {

    @Override
    protected Class<FlagAttribute> getType() {
        return FlagAttribute.class;
    }

    @Override
    protected FlagAttribute decodeValue(MessageHeader messageHeader, ByteBuffer byteBuffer, Integer type, Integer length) {
        return new FlagAttribute(type, length, true);
    }

    @Override
    protected byte[] encodeValue(MessageHeader messageHeader, FlagAttribute messageAttribute) {
        return new byte[0];
    }

}
