package com.gastonlagaf.signaling.repository;

import com.gastonlagaf.signaling.model.Session;

import java.util.List;
import java.util.Optional;

public interface SessionDatastore {

    Optional<Session> findById(String id);

    Session save(Session session);

    Optional<Session> remove(Session session);

}
