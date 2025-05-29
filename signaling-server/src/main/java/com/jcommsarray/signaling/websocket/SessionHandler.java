package com.jcommsarray.signaling.websocket;

import com.jcommsarray.signaling.model.ClosingEvent;
import com.jcommsarray.signaling.model.InviteAnsweredEvent;
import com.jcommsarray.signaling.model.InviteEvent;
import com.jcommsarray.signaling.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@MessageMapping("/sessions")
@RequiredArgsConstructor
public class SessionHandler {

    private final SessionService service;

    @MessageMapping("/create")
    public void create(Principal principal) {
        service.create(principal.getName());
    }

    @MessageMapping("/{id}/destroy")
    public void destroy(Principal principal, @DestinationVariable("id") String id) {
        service.destroy(id, principal.getName());
    }

    @MessageMapping("/{id}/invite")
    public void invite(Principal principal, @DestinationVariable("id") String id, @Payload InviteEvent event) {
        service.invite(id, principal.getName(), event);
    }

    @MessageMapping("/{id}/answer")
    public void answer(Principal principal, @DestinationVariable("id") String id, @Payload InviteAnsweredEvent event) {
        service.answer(id, principal.getName(), event);
    }

    @MessageMapping("/{id}/reject")
    public void reject(Principal principal, @DestinationVariable("id") String id) {
        service.reject(id, principal.getName());
    }

    @MessageMapping("/{id}/acknowledge")
    public void acknowledge(Principal principal, @DestinationVariable("id") String id) {
        service.acknowledge(id, principal.getName());
    }

    @MessageMapping("/{id}/remove")
    public void removeParticipant(Principal principal, @DestinationVariable("id") String id, @Payload ClosingEvent event) {
        service.removeParticipant(id, event.getUserId());
    }

    @MessageMapping("/{id}/leave")
    public void leave(Principal principal, @DestinationVariable("id") String id) {
        service.leave(id, principal.getName());
    }

}
