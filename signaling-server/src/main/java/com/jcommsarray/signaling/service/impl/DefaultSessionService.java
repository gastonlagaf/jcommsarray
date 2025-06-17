package com.jcommsarray.signaling.service.impl;

import com.jcommsarray.signaling.exception.SessionException;
import com.jcommsarray.signaling.model.*;
import com.jcommsarray.signaling.repository.SessionDatastore;
import com.jcommsarray.signaling.service.SessionService;
import com.jcommsarray.signaling.service.SignalingSubscriberService;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.UUID;

@RequiredArgsConstructor
public class DefaultSessionService implements SessionService {

    private final SessionDatastore datastore;

    private final SignalingSubscriberService signalingSubscriberService;

    @Override
    public Session create(String hostId) {
        Session session = new Session(UUID.randomUUID().toString(), hostId, new HashSet<>());
        Session result = datastore.save(session);
        signalingSubscriberService.send(hostId, new SessionCreatedEvent(result.getId(), result.getHostId()));
        return result;
    }

    @Override
    public Session destroy(String id, String hostId) {
        return datastore.findById(id)
                .filter(it -> it.getHostId().equals(hostId))
                .map(it -> {
                    for (String subscriberId : it.getParticipantIds()) {
                        if (subscriberId.equals(hostId)) {
                            continue;
                        }
                        signalingSubscriberService.send(subscriberId, new ClosingEvent(it.getId(), hostId));
                    }
                    datastore.remove(it);
                    return it;
                })
                .orElseThrow(() -> new SessionException(id, hostId, "Session not found"));
    }

    @Override
    public Session invite(String id, String hostId, InviteEvent inviteEvent) {
        return datastore.findById(id)
                .filter(it -> it.getHostId().equals(hostId) || it.getParticipantIds().contains(hostId))
                .map(it -> {
                    getSubscriber(it.getId(), inviteEvent.getUserId());
                    it.getParticipantIds().add(inviteEvent.getUserId());
                    Session result = datastore.save(it);
                    signalingSubscriberService.send(
                            inviteEvent.getUserId(),
                            new InviteEvent(it.getId(), it.getHostId(), inviteEvent.getAddresses(), inviteEvent.getPassword())
                    );
                    return result;
                })
                .orElseThrow(() -> new SessionException(id, hostId, "Session not found"));
    }

    @Override
    public Session answer(String id, String subscriberId, InviteAnsweredEvent event) {
        return datastore.findById(id)
                .filter(it -> it.getParticipantIds().contains(subscriberId))
                .map(it -> {
                    SignalingSubscriber targetSubscriber = new SignalingSubscriber(subscriberId, event.getPassword(), event.getAddresses());
                    InviteAnsweredEvent inviteAnsweredEvent = new InviteAnsweredEvent(
                            it.getId(), targetSubscriber.getId(), targetSubscriber.getAddresses(), event.getPassword()
                    );
                    signalingSubscriberService.send(it.getHostId(), inviteAnsweredEvent);
                    return it;
                })
                .orElseThrow(() -> new SessionException(id, subscriberId, "Session not found"));
    }

    @Override
    public Session reject(String id, String subscriberId) {
        return datastore.findById(id)
                .filter(it -> it.getParticipantIds().contains(subscriberId))
                .map(it -> {
                    it.getParticipantIds().remove(subscriberId);
                    signalingSubscriberService.send(it.getHostId(), new CancelEvent(it.getId(), subscriberId, "Invite rejected"));
                    return it;
                })
                .orElseThrow(() -> new SessionException(id, subscriberId, "Session not found"));
    }

    @Override
    public Session acknowledge(String id, String subscriberId) {
        return datastore.findById(id)
                .map(it -> {
                    signalingSubscriberService.send(subscriberId, new AcknowledgedEvent(it.getId(), it.getHostId()));
                    return it;
                })
                .orElseThrow(() -> new SessionException(id, subscriberId, "Session not found"));
    }

    @Override
    public Session removeParticipant(String id, String subscriberId) {
        return datastore.findById(id)
                .map(it -> {
                    it.getParticipantIds().remove(subscriberId);
                    signalingSubscriberService.send(subscriberId, new ClosingEvent(it.getId(), it.getHostId()));
                    return it;
                })
                .orElseThrow(() -> new SessionException(id, subscriberId, "Session not found"));
    }

    @Override
    public Session leave(String id, String subscriberId) {
        return datastore.findById(id)
                .map(it -> {
                    signalingSubscriberService.send(it.getHostId(), new ClosedEvent(it.getId(), subscriberId));
                    return it;
                })
                .orElseThrow(() -> new SessionException(id, subscriberId, "Session not found"));
    }

    private SignalingSubscriber getSubscriber(String sessionId, String subscriberId) {
        try {
            return signalingSubscriberService.get(subscriberId);
        } catch (NoSuchElementException ex) {
            throw new SessionException(sessionId, subscriberId, "Target subscriber not found", ex);
        }
    }

}
