package com.gastonlagaf.stun.codec.impl;

import com.gastonlagaf.stun.codec.attribute.MessageAttributeCodec;
import com.gastonlagaf.stun.codec.attribute.MessageAttributeCodecContainer;
import com.gastonlagaf.stun.codec.buffer.NonResizableBuffer;
import com.gastonlagaf.stun.codec.util.CodecUtils;
import com.gastonlagaf.stun.integrity.utils.IntegrityUtils;
import com.gastonlagaf.stun.model.*;
import com.gastonlagaf.stun.user.model.UserDetails;
import com.gastonlagaf.udp.codec.CommunicationCodec;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class MessageCodec implements CommunicationCodec<Message> {

    private final MessageAttributeCodecContainer codecContainer;

    private final UserDetails userDetails;

    private final PasswordAlgorithm passwordAlgorithm;

    public MessageCodec() {
        this(null, PasswordAlgorithm.MD5);
    }

    public MessageCodec(UserDetails userDetails, PasswordAlgorithm passwordAlgorithm) {
        this.userDetails = userDetails;
        this.passwordAlgorithm = passwordAlgorithm;
        this.codecContainer = new MessageAttributeCodecContainer();
    }

    @Override
    public ByteBuffer encode(Message message) {
        if (MessageType.INBOUND_CHANNEL_DATA.equals(message.getHeader().getType())) {
            return encodeChannelData(message);
        }

        if (!PasswordAlgorithm.MD5.equals(passwordAlgorithm)) {
            MessageAttribute passwordAlgorithmAttribute = new PasswordAlgorithmAttribute(passwordAlgorithm);
            message.getAttributes().put(passwordAlgorithmAttribute);
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
        Integer typeCode = CodecUtils.readShort(buffer);
        if (ChannelNumberAttribute.MIN_CHANNEL_NUMBER <= typeCode && typeCode <= ChannelNumberAttribute.MAX_CHANNEL_NUMBER) {
            return decodeChannelData(typeCode, buffer);
        }

        MessageHeader header = decodeHeader(typeCode, buffer);

        MessageAttributes attributes = new MessageAttributes(new HashMap<>());
        while (buffer.position() - MessageHeader.LENGTH < header.getLength()) {
            Integer type = CodecUtils.readShort(buffer);

            MessageAttribute messageAttribute = codecContainer.get(type).decode(header, type, buffer);

            attributes.put(messageAttribute);
        }

        return new Message(header, attributes);
    }

    private void encodeHeader(MessageHeader header, NonResizableBuffer dest) {
        dest.write(CodecUtils.shortToByteArray(header.getType().getCode().shortValue()));
        dest.write(CodecUtils.shortToByteArray(header.getLength().shortValue()));
        dest.write(CodecUtils.intToByteArray(header.getMagicCookie()));
        dest.write(header.getTransactionId());
    }

    private MessageHeader decodeHeader(Integer typeCode, ByteBuffer byteBuffer) {
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

    private Message decodeChannelData(Integer number, ByteBuffer byteBuffer) {
        int length = CodecUtils.readShort(byteBuffer);

        byte[] data = new byte[length];
        byteBuffer.get(data);

        MessageHeader messageHeader = new MessageHeader(MessageType.OUTBOUND_CHANNEL_DATA);
        Map<Integer, MessageAttribute> attributes = Map.of(
                KnownAttributeName.CHANNEL_NUMBER.getCode(), new ChannelNumberAttribute(KnownAttributeName.CHANNEL_NUMBER.getCode(), Short.BYTES, number),
                KnownAttributeName.DATA.getCode(), new DefaultMessageAttribute(KnownAttributeName.DATA.getCode(), length, data)
        );
        return new Message(messageHeader, attributes);
    }

    private ByteBuffer encodeChannelData(Message message) {
        Integer channelNumber = message.getAttributes().<ChannelNumberAttribute>get(KnownAttributeName.CHANNEL_NUMBER).getValue();
        byte[] data = message.getAttributes().<DefaultMessageAttribute>get(KnownAttributeName.DATA).getValue();

        ByteBuffer result = ByteBuffer.allocate(Short.BYTES + Short.BYTES + data.length);
        result.put(CodecUtils.shortToByteArray(channelNumber.shortValue()));
        result.put(CodecUtils.shortToByteArray((short) data.length));
        result.put(data);

        result.flip();

        return result;
    }

}
