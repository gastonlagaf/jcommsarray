package com.jcommsarray.turn.integrity.user.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserDetails {

    private final String username;

    private final String password;

    private final String realm;

}
