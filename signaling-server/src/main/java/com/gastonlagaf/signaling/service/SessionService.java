package com.gastonlagaf.signaling.service;

import com.gastonlagaf.signaling.model.Session;

import java.util.List;

public interface SessionService {

    Session create(String hostId);

    Session destroy(String id, String hostId);

    Session invite(String id, String hostId, String subscriberId);

    Session answer(String id, String subscriberId);

    Session reject(String id, String subscriberId);

    Session acknowledge(String id, String subscriberId);

    Session removeParticipant(String id, String subscriberId);

    Session leave(String id, String subscriberId);

}
