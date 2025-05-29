package com.jcommsarray.turn.integrity.integrity.model;

import com.jcommsarray.turn.integrity.user.model.UserDetails;
import com.jcommsarray.turn.model.PasswordAlgorithm;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class IntegrityDetails {

    private final UserDetails userDetails;

    private final PasswordAlgorithm passwordAlgorithm;

}
