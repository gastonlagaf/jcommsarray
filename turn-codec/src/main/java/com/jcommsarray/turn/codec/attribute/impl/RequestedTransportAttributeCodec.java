package com.jcommsarray.turn.codec.attribute.impl;

import com.jcommsarray.turn.codec.attribute.BaseMessageAttributeCodec;
import com.jcommsarray.turn.model.MessageHeader;
import com.jcommsarray.turn.model.Protocol;
import com.jcommsarray.turn.model.RequestedTransportAttribute;

import java.nio.ByteBuffer;
import java.util.stream.IntStream;

public class RequestedTransportAttributeCodec extends BaseMessageAttributeCodec<RequestedTransportAttribute> {

    private static final Integer VALUE_LENGTH = 4;

    private static final Integer RFFU_OFFSET = 3;

    @Override
    protected Class<RequestedTransportAttribute> getType() {
        return RequestedTransportAttribute.class;
    }

    @Override
    protected RequestedTransportAttribute decodeValue(MessageHeader messageHeader, ByteBuffer byteBuffer, Integer type, Integer length) {
        Integer code = (int) byteBuffer.get();
        Protocol protocol = Protocol.ofCode(code);
        byteBuffer.position(byteBuffer.position() + RFFU_OFFSET);
        return new RequestedTransportAttribute(type, VALUE_LENGTH, protocol);
    }

    @Override
    protected byte[] encodeValue(MessageHeader messageHeader, RequestedTransportAttribute messageAttribute) {
        ByteBuffer buffer = ByteBuffer.allocate(VALUE_LENGTH);
        buffer.put(messageAttribute.getValue().getCode().byteValue());
        IntStream.range(0, RFFU_OFFSET).forEach(it -> buffer.put(PADDING));
        return buffer.array();
    }

}
