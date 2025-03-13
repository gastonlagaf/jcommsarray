package com.gastonlagaf.signaling.repository;

import com.gastonlagaf.signaling.model.Session;

import java.util.List;
import java.util.Optional;

public interface SessionDatastore {

    Optional<Session> findById(String id);

    List<Session> findAllByHostId(String hostId);

    Session save(Session session);

    Optional<Session> remove(Session session);

    List<Session> removeAllByHostId(String hostId);

}
