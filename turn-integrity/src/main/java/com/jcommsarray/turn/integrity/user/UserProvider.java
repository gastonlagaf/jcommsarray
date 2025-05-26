package com.jcommsarray.turn.integrity.user;

import com.jcommsarray.turn.integrity.user.model.UserDetails;

import java.util.Optional;

public interface UserProvider {

    Optional<UserDetails> find(String realm, String username);

}
