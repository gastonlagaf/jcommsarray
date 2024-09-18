package com.gastonlagaf.stun.codec.impl;

import com.gastonlagaf.stun.codec.MessageCodec;
import com.gastonlagaf.stun.codec.attribute.MessageAttributeCodec;
import com.gastonlagaf.stun.codec.attribute.MessageAttributeCodecContainer;
import com.gastonlagaf.stun.codec.buffer.NonResizableBuffer;
import com.gastonlagaf.stun.codec.util.CodecUtils;
import com.gastonlagaf.stun.integrity.utils.IntegrityUtils;
import com.gastonlagaf.stun.model.*;
import com.gastonlagaf.stun.user.model.UserDetails;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class DefaultMessageCodec implements MessageCodec {

    private final MessageAttributeCodecContainer codecContainer;

    private final UserDetails userDetails;

    private final PasswordAlgorithm passwordAlgorithm;

    public DefaultMessageCodec() {
        this(null, PasswordAlgorithm.MD5);
    }

    public DefaultMessageCodec(UserDetails userDetails, PasswordAlgorithm passwordAlgorithm) {
        this.userDetails = userDetails;
        this.passwordAlgorithm = passwordAlgorithm;
        this.codecContainer = new MessageAttributeCodecContainer();
    }

    @Override
    public ByteBuffer encode(Message message) {
        if (!PasswordAlgorithm.MD5.equals(passwordAlgorithm)) {
            MessageAttribute passwordAlgorithmAttribute = new PasswordAlgorithmAttribute(passwordAlgorithm);
            message.getAttributes().put(passwordAlgorithmAttribute.getType(), passwordAlgorithmAttribute);
        }

        NonResizableBuffer buffer = new NonResizableBuffer();

        MessageHeader header = new MessageHeader(message.getHeader(), buffer.size());
        encodeHeader(header, buffer);
        for (MessageAttribute attribute: message.getAttributes().values()) {
            MessageAttributeCodec codec = codecContainer.get(attribute.getType());
            codec.encode(message.getHeader(), attribute, buffer);
        }
        encodeIntegrityIfRequired(header, buffer);

        byte[] result = buffer.toByteArray();
        IntegrityUtils.setLength(result, result.length - MessageHeader.LENGTH);
        return ByteBuffer.wrap(result);
    }

    @Override
    public Message decode(ByteBuffer buffer) {
        MessageHeader header = decodeHeader(buffer);

        Map<Integer, MessageAttribute> attributes = new HashMap<>();
        while (buffer.position() - MessageHeader.LENGTH < header.getLength()) {
            Integer type = CodecUtils.readShort(buffer);

            MessageAttribute messageAttribute = codecContainer.get(type).decode(header, type, buffer);

            attributes.put(type, messageAttribute);
        }

        return new Message(header, attributes);
    }

    private void encodeHeader(MessageHeader header, NonResizableBuffer dest) {
        dest.write(CodecUtils.shortToByteArray(header.getType().getCode().shortValue()));
        dest.write(CodecUtils.shortToByteArray(header.getLength().shortValue()));
        dest.write(CodecUtils.intToByteArray(header.getMagicCookie()));
        dest.write(header.getTransactionId());
    }

    private MessageHeader decodeHeader(ByteBuffer byteBuffer) {
        Integer typeCode = CodecUtils.readShort(byteBuffer);
        Integer length = CodecUtils.readShort(byteBuffer);

        Integer magicCookie = byteBuffer.getInt();
        if (!MessageHeader.MAGIC_COOKIE.equals(magicCookie)) {
            throw new IllegalArgumentException("Invalid magic cookie");
        }

        byte[] transactionId = new byte[MessageHeader.TRANSACTION_ID_LENGTH];
        byteBuffer.get(transactionId);

        return new MessageHeader(typeCode, length, transactionId);
    }

    private void encodeIntegrityIfRequired(MessageHeader messageHeader, NonResizableBuffer buffer) {
        if (null == userDetails) {
            return;
        }
        byte[] precedingBytes = buffer.toByteArray();
        MessageIntegrityAttribute messageIntegrityAttribute = new MessageIntegrityAttribute(
                KnownAttributeName.MESSAGE_INTEGRITY_SHA256.getCode(), precedingBytes,
                userDetails.getUsername(), userDetails.getRealm(), userDetails.getPassword(), passwordAlgorithm
        );
        codecContainer.get(messageIntegrityAttribute.getType()).encode(messageHeader, messageIntegrityAttribute, buffer);
    }

}
