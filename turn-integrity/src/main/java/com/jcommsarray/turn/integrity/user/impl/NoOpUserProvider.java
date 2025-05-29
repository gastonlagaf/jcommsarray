package com.jcommsarray.turn.integrity.user.impl;

import com.jcommsarray.turn.integrity.user.UserProvider;
import com.jcommsarray.turn.integrity.user.model.UserDetails;

import java.util.Optional;

public class NoOpUserProvider implements UserProvider {

    private static final String DUMMY_PASSWORD = "dumbAndStupider";

    @Override
    public Optional<UserDetails> find(String realm, String username) {
        UserDetails result = new UserDetails(username, DUMMY_PASSWORD, realm);
        return Optional.of(result);
    }

}
