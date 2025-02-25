package com.gastonlagaf.udp.client.stun.server.codec;

import com.gastonlagaf.udp.client.stun.codec.impl.MessageCodec;
import com.gastonlagaf.udp.client.stun.model.DefaultMessageAttribute;
import com.gastonlagaf.udp.client.stun.model.KnownAttributeName;
import com.gastonlagaf.udp.client.stun.model.Message;
import com.gastonlagaf.udp.client.stun.model.MessageType;

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
