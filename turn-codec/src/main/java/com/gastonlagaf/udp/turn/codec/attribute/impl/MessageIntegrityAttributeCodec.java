package com.gastonlagaf.udp.client.stun.codec.attribute.impl;

import com.gastonlagaf.udp.client.stun.codec.attribute.IntegrityAttributeCodec;
import com.gastonlagaf.udp.client.stun.integrity.utils.IntegrityUtils;
import com.gastonlagaf.udp.client.stun.model.MessageHeader;
import com.gastonlagaf.udp.client.stun.model.MessageIntegrityAttribute;
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
