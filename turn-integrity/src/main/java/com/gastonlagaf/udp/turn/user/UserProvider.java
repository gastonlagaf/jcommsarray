package com.gastonlagaf.udp.client.stun.user;

import com.gastonlagaf.udp.client.stun.user.model.UserDetails;

import java.util.Optional;

public interface UserProvider {

    Optional<UserDetails> find(String realm, String username);

}
