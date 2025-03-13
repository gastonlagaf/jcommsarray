package com.gastonlagaf.signaling.repository.impl;

import com.gastonlagaf.signaling.model.SignalingSubscriber;
import com.gastonlagaf.signaling.repository.SubscriberDatastore;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemorySubscriberDatastore implements SubscriberDatastore {

    private final Map<String, SignalingSubscriber> store = new ConcurrentHashMap<>();

    @Override
    public Optional<SignalingSubscriber> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public SignalingSubscriber save(SignalingSubscriber subscriber) {
        return store.put(subscriber.getId(), subscriber);
    }

    @Override
    public Optional<SignalingSubscriber> remove(SignalingSubscriber subscriber) {
        return Optional.ofNullable(store.remove(subscriber.getId()));
    }

}
