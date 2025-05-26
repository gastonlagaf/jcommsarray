package com.jcommsarray.signaling.service;

import com.jcommsarray.signaling.model.InviteAnsweredEvent;
import com.jcommsarray.signaling.model.InviteEvent;
import com.jcommsarray.signaling.model.Session;

public interface SessionService {

    Session create(String hostId);

    Session destroy(String id, String hostId);

    Session invite(String id, String hostId, InviteEvent inviteEvent);

    Session answer(String id, String subscriberId, InviteAnsweredEvent event);

    Session reject(String id, String subscriberId);

    Session acknowledge(String id, String subscriberId);

    Session removeParticipant(String id, String subscriberId);

    Session leave(String id, String subscriberId);

}
