package com.gastonlagaf.signaling.service;

import com.gastonlagaf.signaling.model.SignalingSubscriber;

import java.util.Optional;

public interface SignalingSubscriberService {

    SignalingSubscriber get(String id);

    SignalingSubscriber save(SignalingSubscriber subscriber);

    Optional<SignalingSubscriber> remove(String id);

}
