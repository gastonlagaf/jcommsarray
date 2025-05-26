package com.jcommsarray.turn.codec.attribute;

import com.jcommsarray.turn.codec.buffer.NonResizableBuffer;
import com.jcommsarray.turn.codec.util.CodecUtils;
import com.jcommsarray.turn.model.MessageAttribute;
import com.jcommsarray.turn.model.MessageHeader;

import java.nio.ByteBuffer;

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
    public void encode(MessageHeader messageHeader, MessageAttribute messageAttribute, NonResizableBuffer dest) {
        Class<T> type = getType();
        if (!type.isAssignableFrom(messageAttribute.getClass())) {
            throw new IllegalArgumentException("Codec got invalid message for encoding: " + messageAttribute.getClass().getSimpleName());
        }
        byte[] encodedValue = this.encodeValue(messageHeader, type.cast(messageAttribute));
        Integer padding = getPaddingLength(encodedValue.length);

        dest.write(CodecUtils.shortToByteArray(messageAttribute.getType().shortValue()));
        dest.write(CodecUtils.shortToByteArray((short) encodedValue.length));
        dest.write(encodedValue);

        dest.pad(padding);
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
