package com.gastonlagaf.stun.codec.attribute;

import com.gastonlagaf.stun.codec.util.CodecUtils;
import com.gastonlagaf.stun.model.MessageAttribute;
import com.gastonlagaf.stun.model.MessageHeader;

import java.nio.ByteBuffer;
import java.util.stream.IntStream;

public abstract class BaseMessageAttributeCodec<T extends MessageAttribute> implements MessageAttributeCodec {

    private static final Integer ATTRIBUTE_HEADER_LENGTH = 4;

    protected static final Integer VALUE_PADDING_LENGTH = 4;

    protected static final Integer NO_PADDING_LENGTH = 0;

    protected static final Byte PADDING = 0;

    @Override
    public MessageAttribute decode(MessageHeader messageHeader, Integer type, ByteBuffer buffer) {
        Integer length = CodecUtils.readShort(buffer);
        MessageAttribute result = this.decodeValue(messageHeader, buffer, type, length);

        Integer padding = getPaddingLength(length);
        buffer.position(buffer.position() + padding);

        return result;
    }

    @Override
    public ByteBuffer encode(MessageHeader messageHeader, MessageAttribute messageAttribute) {
        Class<T> type = getType();
        if (!type.isAssignableFrom(messageAttribute.getClass())) {
            throw new IllegalArgumentException("Codec got invalid message for encoding: " + messageAttribute.getClass().getSimpleName());
        }
        byte[] encodedValue = this.encodeValue(messageHeader, type.cast(messageAttribute));
        Integer padding = getPaddingLength(encodedValue.length);

        ByteBuffer result = ByteBuffer.allocate(ATTRIBUTE_HEADER_LENGTH + encodedValue.length + padding);
        CodecUtils.writeShort(result, messageAttribute.getType());
        CodecUtils.writeShort(result, encodedValue.length);
        result.put(encodedValue);

        IntStream.range(0, padding).forEach(it -> result.put(PADDING));

        return result;
    }

    protected abstract Class<T> getType();

    protected abstract T decodeValue(MessageHeader messageHeader, ByteBuffer byteBuffer, Integer type, Integer length);

    protected abstract byte[] encodeValue(MessageHeader messageHeader, T messageAttribute);

    protected Integer getPaddingLength(Integer size) {
        Integer remainder = size % VALUE_PADDING_LENGTH;
        if (NO_PADDING_LENGTH.equals(remainder)) {
            return NO_PADDING_LENGTH;
        }
        return VALUE_PADDING_LENGTH - remainder;
    }

}
