package com.gastonlagaf.udp.turn.user.impl;

import com.gastonlagaf.udp.turn.user.UserProvider;
import com.gastonlagaf.udp.turn.user.model.UserDetails;

import java.util.Optional;

public class NoOpUserProvider implements UserProvider {

    private static final String DUMMY_PASSWORD = "dumbAndStupider";

    @Override
    public Optional<UserDetails> find(String realm, String username) {
        UserDetails result = new UserDetails(username, DUMMY_PASSWORD, realm);
        return Optional.of(result);
    }

}
