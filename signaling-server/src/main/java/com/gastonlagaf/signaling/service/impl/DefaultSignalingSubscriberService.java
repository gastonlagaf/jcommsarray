package com.gastonlagaf.signaling.service.impl;

import com.gastonlagaf.signaling.model.SignalingSubscriber;
import com.gastonlagaf.signaling.repository.SubscriberDatastore;
import com.gastonlagaf.signaling.service.SignalingSubscriberService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

import java.time.Duration;
import java.util.Optional;

@RequiredArgsConstructor
public class DefaultSignalingSubscriberService implements SignalingSubscriberService {

    private final SubscriberDatastore datastore;

    private final SimpMessageSendingOperations messagingTemplate;

    @Override
    public SignalingSubscriber get(String id) {
        return datastore.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No such subscriber with id: " + id));
    }

    @Override
    public SignalingSubscriber save(SignalingSubscriber subscriber) {
        return datastore.save(subscriber);
    }

    @Override
    public Optional<SignalingSubscriber> remove(String id) {
        return datastore.findById(id).map(it -> {
            datastore.remove(it);
            return it;
        });
    }
}
