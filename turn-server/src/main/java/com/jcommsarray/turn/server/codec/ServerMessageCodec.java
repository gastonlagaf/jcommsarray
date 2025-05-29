package com.jcommsarray.turn.server.codec;

import com.jcommsarray.turn.codec.impl.MessageCodec;
import com.jcommsarray.turn.model.DefaultMessageAttribute;
import com.jcommsarray.turn.model.KnownAttributeName;
import com.jcommsarray.turn.model.Message;
import com.jcommsarray.turn.model.MessageType;

import java.nio.ByteBuffer;
import java.util.Set;

public class ServerMessageCodec extends MessageCodec {

    private final Set<MessageType> SKIP_ENCODE_TYPES = Set.of(
            MessageType.SEND, MessageType.OUTBOUND_CHANNEL_DATA
    );

    @Override
    public ByteBuffer encode(Message message) {
        if (SKIP_ENCODE_TYPES.contains(message.getHeader().getType())) {
            byte[] data = message.getAttributes().<DefaultMessageAttribute>get(KnownAttributeName.DATA).getValue();
            return ByteBuffer.wrap(data);
        }
        return super.encode(message);
    }
}
