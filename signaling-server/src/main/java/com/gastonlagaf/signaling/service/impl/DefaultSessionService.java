package com.gastonlagaf.signaling.service.impl;

import com.gastonlagaf.signaling.model.Session;
import com.gastonlagaf.signaling.repository.SessionDatastore;
import com.gastonlagaf.signaling.service.SessionService;
import com.gastonlagaf.signaling.service.SignalingSubscriberService;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class DefaultSessionService implements SessionService {

    private final SessionDatastore datastore;

    private final SignalingSubscriberService signalingSubscriberService;

    @Override
    public Session create(String hostId) {
        Session session = new Session(UUID.randomUUID().toString(), hostId, List.of());
        return datastore.save(session);
    }

    @Override
    public Session destroy(String id) {
        return datastore.findById(id)
                .map(it -> {
                    datastore.remove(it);
                    return it;
                })
                .orElse(null);
    }

    @Override
    public List<Session> destroyAllByHostId(String hostId) {
        return datastore.findAllByHostId(hostId).stream()
                .peek(datastore::remove)
                .toList();
    }

    @Override
    public Session invite(String id, String subscriberId) {
        return null;
    }

    @Override
    public Session removeParticipant(String id, String subscriberId) {
        return null;
    }

}
