package com.jcommsarray.turn.codec.attribute.impl;

import com.jcommsarray.turn.codec.attribute.BaseMessageAttributeCodec;
import com.jcommsarray.turn.codec.util.CodecUtils;
import com.jcommsarray.turn.model.AddressErrorCodeAttribute;
import com.jcommsarray.turn.model.IpFamily;
import com.jcommsarray.turn.model.MessageHeader;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class AddressErrorCodeAttributeCodec extends BaseMessageAttributeCodec<AddressErrorCodeAttribute> {

    private static final Integer ERROR_CODE_MULTIPLIER = 100;

    private static final Integer FIXED_PART_VALUE_LENGTH = 4;

    @Override
    protected Class<AddressErrorCodeAttribute> getType() {
        return AddressErrorCodeAttribute.class;
    }

    @Override
    protected AddressErrorCodeAttribute decodeValue(MessageHeader messageHeader, ByteBuffer byteBuffer, Integer type, Integer length) {
        Integer ipFamilyCode = (int) byteBuffer.get();
        IpFamily ipFamily = IpFamily.ofCode(ipFamilyCode);

        Integer errorClass = (int) byteBuffer.getShort();
        Integer errorNumber = (int) byteBuffer.get();
        Integer errorCode = errorClass * ERROR_CODE_MULTIPLIER + errorNumber;

        byte[] reasonBytes = new byte[length];
        byteBuffer.get(reasonBytes);
        String reasonPhrase = new String(reasonBytes);

        return new AddressErrorCodeAttribute(type, length, ipFamily, errorCode, reasonPhrase);
    }

    @Override
    protected byte[] encodeValue(MessageHeader messageHeader, AddressErrorCodeAttribute messageAttribute) {
        int errorClass = messageAttribute.getCode() / ERROR_CODE_MULTIPLIER;
        int errorNumber = messageAttribute.getCode() % ERROR_CODE_MULTIPLIER;

        byte[] reasonBytes = messageAttribute.getReasonPhrase().getBytes(StandardCharsets.UTF_8);

        ByteBuffer byteBuffer = ByteBuffer.allocate(FIXED_PART_VALUE_LENGTH + reasonBytes.length);
        byteBuffer.put(messageAttribute.getIpFamily().getCode().byteValue());
        byteBuffer.put(CodecUtils.shortToByteArray((short)errorClass));
        byteBuffer.put((byte) errorNumber);
        byteBuffer.put(reasonBytes);

        return byteBuffer.array();
    }

}
