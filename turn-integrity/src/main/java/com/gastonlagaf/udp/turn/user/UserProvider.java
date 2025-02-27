package com.gastonlagaf.udp.turn.user;

import com.gastonlagaf.udp.turn.user.model.UserDetails;

import java.util.Optional;

public interface UserProvider {

    Optional<UserDetails> find(String realm, String username);

}
