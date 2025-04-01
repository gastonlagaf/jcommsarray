package com.gastonlagaf.signaling.repository.impl;

import com.gastonlagaf.signaling.model.Session;
import com.gastonlagaf.signaling.repository.SessionDatastore;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.time.Duration;
import java.util.Optional;

public class InMemorySessionDatastore implements SessionDatastore {

    private final Cache<String, Session> store = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(5))
            .build();

    @Override
    public Optional<Session> findById(String id) {
        return Optional.ofNullable(store.getIfPresent(id));
    }

    @Override
    public Session save(Session session) {
        store.put(session.getId(), session);
        return session;
    }

    @Override
    public Optional<Session> remove(Session session) {
        store.invalidate(session.getId());
        return Optional.of(session);
    }

}
