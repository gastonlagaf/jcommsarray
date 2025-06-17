package com.jcommsarray.client.ice.auth;

import com.jcommsarray.turn.integrity.user.UserProvider;
import com.jcommsarray.turn.integrity.user.model.UserDetails;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class IceUserProvider implements UserProvider {

    private UserDetails targetUser;

    public void setTargetUser(UserDetails targetUser) {
        if (null != this.targetUser) {
            throw new IllegalStateException("Target user is already set");
        }
        this.targetUser = targetUser;
    }

    @Override
    public Optional<UserDetails> find(String realm, String username) {
        return Optional.ofNullable(targetUser)
                .filter(it -> it.getRealm().equals(realm))
                .filter(it -> it.getUsername().equals(username));
    }

}
