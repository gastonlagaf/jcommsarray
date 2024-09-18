package com.gastonlagaf.stun.codec.attribute;

import com.gastonlagaf.stun.codec.buffer.NonResizableBuffer;
import com.gastonlagaf.stun.model.MessageAttribute;
import com.gastonlagaf.stun.model.MessageHeader;

import java.nio.ByteBuffer;

public interface MessageAttributeCodec {

    MessageAttribute decode(MessageHeader messageHeader, Integer type, ByteBuffer buffer);

    void encode(MessageHeader messageHeader, MessageAttribute messageAttribute, NonResizableBuffer dest);

}
