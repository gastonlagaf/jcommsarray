package com.gastonlagaf.udp.codec;

import java.nio.ByteBuffer;

public interface CommunicationCodec<T> {

    ByteBuffer encode(T message);

    T decode(ByteBuffer buffer);

}
