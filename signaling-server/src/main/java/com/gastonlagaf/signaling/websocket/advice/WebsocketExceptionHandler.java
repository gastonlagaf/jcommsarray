package com.gastonlagaf.signaling.websocket.advice;

import com.gastonlagaf.signaling.exception.SessionException;
import com.gastonlagaf.signaling.model.CancelEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.ControllerAdvice;

@Slf4j
@ControllerAdvice
public class WebsocketExceptionHandler {

    @MessageExceptionHandler(SessionException.class)
    @SendToUser(destinations = "/events", broadcast = false)
    public CancelEvent handleSessionException(SessionException ex) {
        log.error("Exception during session processing", ex);
        return new CancelEvent(ex.getSessionId(), ex.getSubscriberId(), ex.getMessage());
    }

}
