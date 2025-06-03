package com.jcommsarray.client.signaling.stomp.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StompHeaders {

    public static final String DESTINATION = "destination";

    public static final String ID = "id";

    public static final String ACCEPT_VERSION = "accept-version";

    public static final String CONTENT_TYPE = "content-type";

    public static final String HOST = "host";

}
