package com.jcommsarray.signaling.repository;

import com.jcommsarray.signaling.model.SignalingSubscriber;

import java.util.Optional;

public interface SubscriberDatastore {

    Optional<SignalingSubscriber> findById(String id);

    SignalingSubscriber save(SignalingSubscriber subscriber);

    Optional<SignalingSubscriber> remove(SignalingSubscriber subscriber);

}
