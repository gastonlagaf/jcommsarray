package com.gastonlagaf.signaling.service;

import com.gastonlagaf.signaling.model.Session;

import java.util.List;

public interface SessionService {

    Session create(String hostId);

    Session destroy(String id);

    List<Session> destroyAllByHostId(String hostId);

    Session invite(String id, String subscriberId);

    Session removeParticipant(String id, String subscriberId);

}
