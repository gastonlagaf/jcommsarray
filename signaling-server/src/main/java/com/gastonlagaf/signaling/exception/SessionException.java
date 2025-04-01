package com.gastonlagaf.signaling.exception;

import lombok.Getter;

@Getter
public class SessionException extends RuntimeException {

    private final String sessionId;

    private final String subscriberId;

    public SessionException(String sessionId, String subscriberId, String message) {
        super(message);
        this.sessionId = sessionId;
        this.subscriberId = subscriberId;
    }

    public SessionException(String sessionId, String subscriberId, String message, Throwable cause) {
        super(message, cause);
        this.sessionId = sessionId;
        this.subscriberId = subscriberId;
    }

}
