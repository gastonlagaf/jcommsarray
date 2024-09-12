package com.gastonlagaf.stun.user;

import com.gastonlagaf.stun.user.model.UserDetails;

import java.util.Optional;

public interface UserProvider {

    Optional<UserDetails> find(String realm, String username);

}
