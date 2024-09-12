package com.gastonlagaf.stun.codec;

import com.gastonlagaf.stun.model.Message;

import java.nio.ByteBuffer;

public interface MessageCodec {

    ByteBuffer encode(Message message);

    Message decode(ByteBuffer buffer);

}
