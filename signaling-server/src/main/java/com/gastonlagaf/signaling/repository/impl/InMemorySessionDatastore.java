package com.gastonlagaf.signaling.repository.impl;

import com.gastonlagaf.signaling.model.Session;
import com.gastonlagaf.signaling.repository.SessionDatastore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemorySessionDatastore implements SessionDatastore {

    private final Map<String, Session> byIdStore = new ConcurrentHashMap<>();

    private final Map<String, List<Session>> byHostStore = new ConcurrentHashMap<>();

    @Override
    public Optional<Session> findById(String id) {
        return Optional.ofNullable(byIdStore.get(id));
    }

    @Override
    public List<Session> findAllByHostId(String hostId) {
        return Optional.ofNullable(byHostStore.get(hostId)).orElse(List.of());
    }

    @Override
    public Session save(Session session) {
        byHostStore.computeIfAbsent(session.getHostId(), k -> new ArrayList<>()).add(session);
        return byIdStore.put(session.getHostId(), session);
    }

    @Override
    public Optional<Session> remove(Session session) {
        List<Session> hostSessions = byHostStore.get(session.getHostId());
        if (null == hostSessions) {
            return Optional.empty();
        }
        hostSessions.remove(session);
        return Optional.ofNullable(byIdStore.remove(session.getId()));
    }

    @Override
    public List<Session> removeAllByHostId(String hostId) {
        List<Session> hostSessions = byHostStore.get(hostId);
        hostSessions.forEach(session -> byIdStore.remove(hostId));
        byHostStore.remove(hostId);
        return hostSessions;
    }

}
