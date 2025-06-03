package com.jcommsarray.signaling.stomp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jcommsarray.client.signaling.stomp.StompCodec;
import com.jcommsarray.client.signaling.stomp.StompPayloadCodec;
import com.jcommsarray.signaling.model.AddressCandidate;
import com.jcommsarray.signaling.model.InviteEvent;
import com.jcommsarray.signaling.model.SignalingEvent;
import com.jcommsarray.client.signaling.stomp.model.StompMessage;
import com.jcommsarray.client.signaling.stomp.model.StompMessageType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

public class StompCodecTests {

    private static final String BASE_EVENT = "SEND\nbobs:office\nin:the\n\n{\"type\":\"INVITE\",\"sessionId\":\"1\",\"userId\":\"2\",\"addresses\":[{\"priority\":1,\"value\":\"127.0.0.1:2022\"},{\"priority\":2,\"value\":\"192.168.0.104:2023\"}]}\u0000";

    private final StompCodec codec = new StompCodec(new StompPayloadCodec() {

        private final ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());

        @Override
        public String encode(Object payload) {
            try {
                return objectMapper.writeValueAsString(payload);
            } catch (JsonProcessingException e) {
                throw new UncheckedIOException(e);
            }
        }

        @Override
        public <T> T decode(String payload, Class<T> clazz) {
            try {
                return objectMapper.readValue(payload, clazz);
            } catch (JsonProcessingException e) {
                throw new UncheckedIOException(e);
            }
        }
    });

    @Test
    void testEncode() {
        StompMessage<InviteEvent> stompMessage = new StompMessage<>(
                StompMessageType.SEND,
                Map.of("in", "the", "bobs", "office"),
                new InviteEvent("1", "2", List.of(
                        new AddressCandidate(1, "HOST", InetSocketAddress.createUnresolved("127.0.0.1", 2022)),
                        new AddressCandidate(2, "HOST", InetSocketAddress.createUnresolved("192.168.0.104", 2023))
                ))
        );
        String result = codec.encode(stompMessage);

        Assertions.assertEquals(BASE_EVENT.length(), result.length());
    }

    @Test
    void testDecode() {
        String raw = "SEND\nbobs:office\nin:the\n\n{\"type\":\"INVITE\",\"sessionId\":\"1\",\"userId\":\"2\",\"addresses\":[{\"priority\":1,\"value\":\"127.0.0.1:2022\"},{\"priority\":2,\"value\":\"192.168.0.104:2023\"}]}\u0000";

        StompMessage<SignalingEvent> result = codec.decode(raw, SignalingEvent.class);

        Assertions.assertEquals("SEND", result.getType());
        Assertions.assertEquals(2, result.getHeaders().size());
    }

}
