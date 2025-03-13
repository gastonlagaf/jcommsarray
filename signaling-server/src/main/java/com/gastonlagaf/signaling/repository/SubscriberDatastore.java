package com.gastonlagaf.signaling.repository;

import com.gastonlagaf.signaling.model.SignalingSubscriber;

import java.time.Duration;
import java.util.Optional;

public interface SubscriberDatastore {

    Optional<SignalingSubscriber> findById(String id);

    SignalingSubscriber save(SignalingSubscriber subscriber);

    Optional<SignalingSubscriber> remove(SignalingSubscriber subscriber);

}
