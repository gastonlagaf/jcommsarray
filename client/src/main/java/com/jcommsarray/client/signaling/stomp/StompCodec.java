package com.jcommsarray.client.signaling.stomp;

import com.jcommsarray.client.signaling.stomp.model.StompMessage;
import com.jcommsarray.client.signaling.stomp.model.StompMessageType;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class StompCodec {

    private static final Character NEWLINE = '\n';

    private static final Character COLON = ':';

    private static final Character STOMP_END = '\0';

    private final StompPayloadCodec payloadCodec;

    public String encode(StompMessage<?> message) {
        StringBuilder buffer = new StringBuilder(message.getType().name());
        buffer.append(NEWLINE);

        encodeHeaders(message.getHeaders(), buffer);
        buffer.append(NEWLINE);

        if (null != message.getPayload()) {
            String serializedPayload = payloadCodec.encode(message.getPayload());
            buffer.append(serializedPayload);
        }
        buffer.append(STOMP_END);

        return buffer.toString();
    }

    public <T> StompMessage<T> decode(CharSequence raw, Class<T> payloadType) {
        CharSequenceReader reader = new CharSequenceReader(raw);
        String typeAsString = reader.readUntil(NEWLINE);
        StompMessageType type = StompMessageType.valueOf(typeAsString);
        Map<String, String> headers = decodeHeaders(reader);
        String payloadStr = reader.readUntil('\0');
        T payload = null != payloadStr
                ? payloadCodec.decode(payloadStr, payloadType)
                : null;
        return new StompMessage<>(type, headers, payload);
    }

    private void encodeHeaders(Map<String, String> headers, StringBuilder buffer) {
        for (Map.Entry<String, String> header : headers.entrySet()) {
            String encodedHeader = header.getKey() + COLON + header.getValue() + NEWLINE;
            buffer.append(encodedHeader);
        }
    }

    private Map<String, String> decodeHeaders(CharSequenceReader reader) {
        Map<String, String> headers = new HashMap<>();
        String line;
        while (null != (line = reader.readUntil(NEWLINE))) {
            if (NEWLINE.toString().equals(line)) {
                break;
            }
            String[] parts = line.split(COLON.toString());
            headers.put(parts[0], parts[1]);
        }

        return headers;
    }

    @RequiredArgsConstructor
    private static class CharSequenceReader {

        private final CharSequence charSequence;

        private int index = 0;

        public String readUntil(Character stopSymbol) {
            StringBuilder result = new StringBuilder();

            char next;
            do {
                next = charSequence.charAt(index);
                if ((null != stopSymbol && stopSymbol.equals(next))) {
                    index++;
                    break;
                }
                result.append(next);
                index++;
            } while (index < charSequence.length());

            return !result.isEmpty() ? result.toString() : null;
        }

    }

}
