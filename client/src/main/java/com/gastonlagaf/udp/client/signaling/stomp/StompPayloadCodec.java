package com.gastonlagaf.udp.client.signaling.stomp;

public interface StompPayloadCodec {

    String encode(Object payload);

    <T> T decode(String payload, Class<T> clazz);

}
