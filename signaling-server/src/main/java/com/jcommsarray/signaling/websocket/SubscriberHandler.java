package com.jcommsarray.signaling.websocket;

import com.jcommsarray.signaling.model.RegisterEvent;
import com.jcommsarray.signaling.model.SignalingSubscriber;
import com.jcommsarray.signaling.service.SignalingSubscriberService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@MessageMapping("/subscriptions")
@RequiredArgsConstructor
public class SubscriberHandler {

    private final SignalingSubscriberService service;

    @MessageMapping("/register")
    public void register(Principal principal, @Payload RegisterEvent event) {
        SignalingSubscriber subscriber = new SignalingSubscriber(principal.getName(), event.getCandidates());
        service.save(subscriber);
    }

    @MessageMapping("/deregister")
    public void deregister(Principal principal) {
        service.remove(principal.getName());
    }

}
