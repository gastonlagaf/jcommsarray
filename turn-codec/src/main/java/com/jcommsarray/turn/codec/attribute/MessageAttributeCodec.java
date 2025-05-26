package com.jcommsarray.turn.codec.attribute;

import com.jcommsarray.turn.codec.buffer.NonResizableBuffer;
import com.jcommsarray.turn.model.MessageAttribute;
import com.jcommsarray.turn.model.MessageHeader;

import java.nio.ByteBuffer;

public interface MessageAttributeCodec {

    MessageAttribute decode(MessageHeader messageHeader, Integer type, ByteBuffer buffer);

    void encode(MessageHeader messageHeader, MessageAttribute messageAttribute, NonResizableBuffer dest);

}
