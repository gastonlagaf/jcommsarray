package com.gastonlagaf.udp.client.stun.codec.attribute.impl;

import com.gastonlagaf.udp.client.stun.codec.attribute.BaseMessageAttributeCodec;
import com.gastonlagaf.udp.client.stun.model.ChangeRequestAttribute;
import com.gastonlagaf.udp.client.stun.model.MessageHeader;

import java.nio.ByteBuffer;

public class ChangeRequestAttributeCodec extends BaseMessageAttributeCodec<ChangeRequestAttribute> {

    @Override
    protected Class<ChangeRequestAttribute> getType() {
        return ChangeRequestAttribute.class;
    }

    @Override
    protected ChangeRequestAttribute decodeValue(MessageHeader messageHeader, ByteBuffer byteBuffer, Integer type, Integer length) {
        byte[] bytes = new byte[length];
        byteBuffer.get(bytes);
        return new ChangeRequestAttribute(bytes);
    }

    @Override
    protected byte[] encodeValue(MessageHeader messageHeader, ChangeRequestAttribute messageAttribute) {
        return messageAttribute.getEncodedValue();
    }

}
