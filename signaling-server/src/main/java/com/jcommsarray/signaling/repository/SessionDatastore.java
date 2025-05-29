package com.jcommsarray.signaling.repository;

import com.jcommsarray.signaling.model.Session;

import java.util.Optional;

public interface SessionDatastore {

    Optional<Session> findById(String id);

    Session save(Session session);

    Optional<Session> remove(Session session);

}
