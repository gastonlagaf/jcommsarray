package com.gastonlagaf.udp.client.stun.codec.attribute.impl;

import com.gastonlagaf.udp.client.stun.codec.attribute.BaseMessageAttributeCodec;
import com.gastonlagaf.udp.client.stun.model.DontFragmentAttribute;
import com.gastonlagaf.udp.client.stun.model.MessageHeader;
import lombok.Getter;

import java.nio.ByteBuffer;

@Getter
public class DontFragmentAttributeCodec extends BaseMessageAttributeCodec<DontFragmentAttribute> {

    private static final Integer VALUE_LENGTH = 0;

    @Override
    protected Class<DontFragmentAttribute> getType() {
        return DontFragmentAttribute.class;
    }

    @Override
    protected DontFragmentAttribute decodeValue(MessageHeader messageHeader, ByteBuffer byteBuffer, Integer type, Integer length) {
        return new DontFragmentAttribute(type, VALUE_LENGTH);
    }

    @Override
    protected byte[] encodeValue(MessageHeader messageHeader, DontFragmentAttribute messageAttribute) {
        return new byte[VALUE_LENGTH];
    }

}
