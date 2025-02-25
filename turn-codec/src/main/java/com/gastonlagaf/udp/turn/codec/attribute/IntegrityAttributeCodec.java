package com.gastonlagaf.udp.client.stun.codec.attribute;

import com.gastonlagaf.udp.client.stun.model.MessageAttribute;

import java.nio.ByteBuffer;

public abstract class IntegrityAttributeCodec<T extends MessageAttribute> extends BaseMessageAttributeCodec<T> {

    private static final Integer START_POSITION = 0;

    protected static final Integer TYPE_AND_LENGTH_OFFSET = 4;

    protected byte[] getPrecedingBytes(ByteBuffer buffer) {
        int currentPosition = buffer.position();
        buffer.position(START_POSITION);

        byte[] result = new byte[currentPosition - TYPE_AND_LENGTH_OFFSET];
        buffer.get(result);

        buffer.position(currentPosition);

        return result;
    }

}
