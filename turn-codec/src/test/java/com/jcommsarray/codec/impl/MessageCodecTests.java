package com.jcommsarray.codec.impl;

import com.jcommsarray.codec.CommunicationCodec;
import com.jcommsarray.turn.codec.attribute.impl.MessageIntegrityAttributeCodec;
import com.jcommsarray.turn.codec.buffer.NonResizableBuffer;
import com.jcommsarray.turn.codec.impl.MessageCodec;
import com.jcommsarray.turn.model.*;
import com.jcommsarray.udp.turn.model.*;
import com.jcommsarray.turn.integrity.user.model.UserDetails;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;

public class MessageCodecTests {

    @Test
    void test() {
        UserDetails userDetails = new UserDetails("\u30DE\u30C8\u30EA\u30C3\u30AF\u30B9", "TheMatrIX", "example.org");
        CommunicationCodec<Message> messageCodec = new MessageCodec(userDetails, PasswordAlgorithm.SHA256);

        MessageHeader header = new MessageHeader(MessageType.BINDING_REQUEST);
        Map<Integer, MessageAttribute> map = getIntegerMessageAttributeMap();

        Message message = new Message(header, map);

        ByteBuffer buffer = messageCodec.encode(message);

        Message newMessage = messageCodec.decode(buffer);

        Assertions.assertEquals(
                new String(message.getHeader().getTransactionId()),
                new String(newMessage.getHeader().getTransactionId())
        );
        Assertions.assertEquals(
                message.getHeader().getType(),
                newMessage.getHeader().getType()
        );
        Assertions.assertEquals(
                5,
                newMessage.getAttributes().size()
        );
    }

    @Test
    void testVector() {
        UserDetails userDetails = new UserDetails("\u30DE\u30C8\u30EA\u30C3\u30AF\u30B9", "TheMatrIX", "example.org");
        CommunicationCodec<Message> messageCodec = new MessageCodec(userDetails, PasswordAlgorithm.MD5);

        byte[] message = HexFormat.of().parseHex(
                "000100902112a44278ad3433c6ad72c029da412e001e00204a3cf38fef6992bda952c6780417da0f24819415569e60b" +
                        "205c46e41407f1704001500296f624d61744a6f733241414143662f2f3439396b39353464364f4c33346f4c3946535" +
                        "47679363473410000000014000b6578616d706c652e6f726700001d000400020000001c0020b5c7bf005b6c52a21c5" +
                        "1c5e892f81924136296cb927c43149309278cc6518e65"
        );

        Message decodedMessage = messageCodec.decode(ByteBuffer.wrap(message));

        MessageIntegrityAttribute decodedAttribute = decodedMessage.getAttributes().get(KnownAttributeName.MESSAGE_INTEGRITY_SHA256);
        MessageIntegrityAttribute attribute = new MessageIntegrityAttribute(
                KnownAttributeName.MESSAGE_INTEGRITY_SHA256.getCode(), decodedAttribute.getPrecedingBytes(),
                userDetails.getUsername(), userDetails.getRealm(), userDetails.getPassword(), PasswordAlgorithm.SHA256
        );
        NonResizableBuffer buffer = new NonResizableBuffer();
        new MessageIntegrityAttributeCodec().encode(decodedMessage.getHeader(), attribute, buffer);
        byte[] encodedAttribute = buffer.toByteArray();
        byte[] encodedPrecededBytes = Arrays.copyOfRange(encodedAttribute, 4, encodedAttribute.length);

        Assertions.assertArrayEquals(decodedAttribute.getValue(), encodedPrecededBytes);

        Assertions.assertEquals(
                new String(decodedMessage.getAttributes().<DefaultMessageAttribute>get(KnownAttributeName.REALM).getValue()),
                "example.org"
        );
        Assertions.assertEquals(
                new String((decodedMessage.getAttributes().<DefaultMessageAttribute>get(21)).getValue()),
                "obMatJos2AAACf//499k954d6OL34oL9FSTvy64sA");
    }

    private static Map<Integer, MessageAttribute> getIntegerMessageAttributeMap() {
        MessageAttribute attribute1 = new DefaultMessageAttribute(KnownAttributeName.USERNAME.getCode(), 0, "vova".getBytes());
        MessageAttribute attribute2 = new AddressAttribute(KnownAttributeName.MAPPED_ADDRESS.getCode(), 0, true, IpFamily.IPV4, 1234, "127.0.0.1");
        MessageAttribute attribute3 = new AddressAttribute(KnownAttributeName.XOR_MAPPED_ADDRESS.getCode(), 0, false, IpFamily.IPV4, 5555, "192.168.0.1");

        return new HashMap<>() {
            {
                put(attribute1.getType(), attribute1);
                put(attribute2.getType(), attribute2);
                put(attribute3.getType(), attribute3);
            }
        };

    }

}
