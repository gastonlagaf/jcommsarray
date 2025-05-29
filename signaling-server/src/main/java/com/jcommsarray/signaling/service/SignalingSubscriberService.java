package com.jcommsarray.signaling.service;

import com.jcommsarray.signaling.model.SignalingEvent;
import com.jcommsarray.signaling.model.SignalingSubscriber;

import java.util.Optional;

public interface SignalingSubscriberService {

    SignalingSubscriber get(String id);

    SignalingSubscriber save(SignalingSubscriber subscriber);

    Optional<SignalingSubscriber> remove(String id);

    void send(String subscriberId, SignalingEvent event);

}
