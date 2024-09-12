package com.gastonlagaf.stun.codec.attribute.impl;

import com.gastonlagaf.stun.codec.attribute.BaseMessageAttributeCodec;
import com.gastonlagaf.stun.codec.util.CodecUtils;
import com.gastonlagaf.stun.model.KnownAttributeName;
import com.gastonlagaf.stun.model.MessageHeader;
import com.gastonlagaf.stun.model.PasswordAlgorithm;
import com.gastonlagaf.stun.model.PasswordAlgorithmAttribute;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.stream.IntStream;

public class PasswordAlgorithmAttributeCodec extends BaseMessageAttributeCodec<PasswordAlgorithmAttribute> {

    @Override
    protected Class<PasswordAlgorithmAttribute> getType() {
        return PasswordAlgorithmAttribute.class;
    }

    @Override
    protected PasswordAlgorithmAttribute decodeValue(MessageHeader messageHeader, ByteBuffer byteBuffer, Integer type, Integer length) {
        int passwordAlgorithmCode = CodecUtils.readShort(byteBuffer);
        PasswordAlgorithm passwordAlgorithm = PasswordAlgorithm.ofCode(passwordAlgorithmCode);

        int parametersLength = CodecUtils.readShort(byteBuffer);
        byteBuffer.position(byteBuffer.position() + parametersLength);

        return new PasswordAlgorithmAttribute(type, length, passwordAlgorithm);
    }

    @Override
    protected byte[] encodeValue(MessageHeader messageHeader, PasswordAlgorithmAttribute messageAttribute) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] serializedAlgorithmCode = CodecUtils.shortToByteArray(messageAttribute.getValue().getCode().shortValue());
        baos.writeBytes(serializedAlgorithmCode);
        IntStream.range(0, Short.BYTES).forEach(it -> baos.write(PADDING));
        return baos.toByteArray();
    }

}
