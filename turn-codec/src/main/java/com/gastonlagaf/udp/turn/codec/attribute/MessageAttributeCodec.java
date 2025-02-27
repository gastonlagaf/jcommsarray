package com.gastonlagaf.udp.turn.codec.attribute;

import com.gastonlagaf.udp.turn.codec.buffer.NonResizableBuffer;
import com.gastonlagaf.udp.turn.model.MessageAttribute;
import com.gastonlagaf.udp.turn.model.MessageHeader;

import java.nio.ByteBuffer;

public interface MessageAttributeCodec {

    MessageAttribute decode(MessageHeader messageHeader, Integer type, ByteBuffer buffer);

    void encode(MessageHeader messageHeader, MessageAttribute messageAttribute, NonResizableBuffer dest);

}
