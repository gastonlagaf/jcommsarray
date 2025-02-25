package com.gastonlagaf.udp.client.stun.codec.attribute;

import com.gastonlagaf.udp.client.stun.codec.buffer.NonResizableBuffer;
import com.gastonlagaf.udp.client.stun.model.MessageAttribute;
import com.gastonlagaf.udp.client.stun.model.MessageHeader;

import java.nio.ByteBuffer;

public interface MessageAttributeCodec {

    MessageAttribute decode(MessageHeader messageHeader, Integer type, ByteBuffer buffer);

    void encode(MessageHeader messageHeader, MessageAttribute messageAttribute, NonResizableBuffer dest);

}
