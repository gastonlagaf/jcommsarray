package com.gastonlagaf.signaling.websocket;

import com.gastonlagaf.signaling.service.SignalingSubscriberService;
import com.gastonlagaf.signaling.websocket.model.InviteRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class SessionHandler {

    private final SignalingSubscriberService service;

    private final SimpMessageSendingOperations messagingTemplate;

    @SubscribeMapping
    public void testSubscribe(MessageHeaders headers) {
        System.out.println();
    }

    @MessageMapping("/test")
    public void test(MessageHeaders headers) {
        System.out.println();
        messagingTemplate.convertAndSend("/ws/events/op", "Nu Zdarova");
    }

    @MessageMapping("/sessions/create")
    public void createSession(Principal principal) {
        System.out.println();
    }

    @MessageMapping("/sessions/{id}/destroy")
    public void destroySession(Principal principal) {

    }

    @MessageMapping("/sessions/{id}/invite")
    public void invite(Principal principal, @Payload InviteRequest inviteRequest) {

    }

}
