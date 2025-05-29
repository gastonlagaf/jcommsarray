package com.jcommsarray.turn.codec.attribute.impl;

import com.jcommsarray.turn.codec.attribute.BaseMessageAttributeCodec;
import com.jcommsarray.turn.codec.util.CodecUtils;
import com.jcommsarray.turn.model.ChannelNumberAttribute;
import com.jcommsarray.turn.model.MessageHeader;

import java.nio.ByteBuffer;
import java.util.stream.IntStream;

public class ChannelNumberAttributeCodec extends BaseMessageAttributeCodec<ChannelNumberAttribute> {

    private static final Integer VALUE_LENGTH = 4;

    private static final Integer RFFU_OFFSET = 2;

    @Override
    protected Class<ChannelNumberAttribute> getType() {
        return ChannelNumberAttribute.class;
    }

    @Override
    protected ChannelNumberAttribute decodeValue(MessageHeader messageHeader, ByteBuffer byteBuffer, Integer type, Integer length) {
        Integer channelNumber = CodecUtils.readShort(byteBuffer);
        byteBuffer.position(byteBuffer.position() + RFFU_OFFSET);
        return new ChannelNumberAttribute(type, VALUE_LENGTH, channelNumber);
    }

    @Override
    protected byte[] encodeValue(MessageHeader messageHeader, ChannelNumberAttribute messageAttribute) {
        ByteBuffer result = ByteBuffer.allocate(VALUE_LENGTH);
        result.put(CodecUtils.shortToByteArray(messageAttribute.getValue().shortValue()));
        IntStream.range(0, RFFU_OFFSET).forEach(it -> result.put(PADDING));
        return result.array();
    }

}
