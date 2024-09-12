package com.gastonlagaf.stun.codec.attribute;

import com.gastonlagaf.stun.model.MessageAttribute;
import com.gastonlagaf.stun.model.MessageHeader;

import java.nio.ByteBuffer;

public interface MessageAttributeCodec {

    MessageAttribute decode(MessageHeader messageHeader, Integer type, ByteBuffer buffer);

    ByteBuffer encode(MessageHeader messageHeader, MessageAttribute messageAttribute);

}
