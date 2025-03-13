package com.gastonlagaf.signaling.websocket;

import com.gastonlagaf.signaling.model.SignalingSubscriber;
import com.gastonlagaf.signaling.service.SignalingSubscriberService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class SubscriberHandler {

    private final SignalingSubscriberService service;

    @MessageMapping("/register")
    public void register(Principal principal, @Payload SignalingSubscriber subscriber) {

    }

    @MessageMapping("/deregister")
    public void deregister(Principal principal) {

    }

}
