package com.gastonlagaf.udp.turn.codec.attribute.impl;

import com.gastonlagaf.udp.turn.codec.attribute.BaseMessageAttributeCodec;
import com.gastonlagaf.udp.turn.model.AddressFamilyAttribute;
import com.gastonlagaf.udp.turn.model.IpFamily;
import com.gastonlagaf.udp.turn.model.MessageHeader;

import java.nio.ByteBuffer;
import java.util.stream.IntStream;

public class AddressFamilyAttributeCodec extends BaseMessageAttributeCodec<AddressFamilyAttribute> {

    private static final Integer VALUE_LENGTH = 4;

    private static final Integer RFFU_OFFSET = 3;

    @Override
    protected Class<AddressFamilyAttribute> getType() {
        return AddressFamilyAttribute.class;
    }

    @Override
    protected AddressFamilyAttribute decodeValue(MessageHeader messageHeader, ByteBuffer byteBuffer, Integer type, Integer length) {
        Integer ipFamilyCode = (int) byteBuffer.get();
        IpFamily ipFamily = IpFamily.ofCode(ipFamilyCode);
        byteBuffer.position(byteBuffer.position() + RFFU_OFFSET);
        return new AddressFamilyAttribute(type, VALUE_LENGTH, ipFamily);
    }

    @Override
    protected byte[] encodeValue(MessageHeader messageHeader, AddressFamilyAttribute messageAttribute) {
        ByteBuffer buffer = ByteBuffer.allocate(VALUE_LENGTH);
        buffer.put(messageAttribute.getValue().getCode().byteValue());
        IntStream.range(0, RFFU_OFFSET).forEach(i -> buffer.put(PADDING));
        return buffer.array();
    }

}
