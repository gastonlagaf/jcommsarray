package com.jcommsarray.turn.codec.attribute.impl;

import com.jcommsarray.turn.codec.attribute.IntegrityAttributeCodec;
import com.jcommsarray.turn.integrity.integrity.utils.IntegrityUtils;
import com.jcommsarray.turn.model.MessageHeader;
import com.jcommsarray.turn.model.MessageIntegrityAttribute;
import lombok.RequiredArgsConstructor;

import java.nio.ByteBuffer;

@RequiredArgsConstructor
public class MessageIntegrityAttributeCodec extends IntegrityAttributeCodec<MessageIntegrityAttribute> {

    @Override
    protected Class<MessageIntegrityAttribute> getType() {
        return MessageIntegrityAttribute.class;
    }

    @Override
    protected byte[] encodeValue(MessageHeader messageHeader, MessageIntegrityAttribute messageAttribute) {
        if (null == messageAttribute.getUsername()) {
            return new byte[0];
        }

        byte[] key = IntegrityUtils.constructKey(
                messageAttribute.getPasswordAlgorithm(),
                messageAttribute.getUsername(),
                messageAttribute.getRealm(),
                messageAttribute.getPassword()
        );

        return IntegrityUtils.constructHash(messageAttribute.getPrecedingBytes(), key, messageAttribute.getIsSha256());
    }

    @Override
    protected MessageIntegrityAttribute decodeValue(MessageHeader messageHeader, ByteBuffer byteBuffer, Integer type, Integer length) {
        byte[] precedingBytes = getPrecedingBytes(byteBuffer);

        byte[] value = new byte[length];
        byteBuffer.get(value);

        return new MessageIntegrityAttribute(type, value, precedingBytes);
    }

}
