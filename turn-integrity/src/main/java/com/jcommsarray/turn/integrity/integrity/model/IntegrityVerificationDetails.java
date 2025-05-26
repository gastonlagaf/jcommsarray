package com.jcommsarray.turn.integrity.integrity.model;

import com.jcommsarray.turn.model.MessageIntegrityAttribute;
import com.jcommsarray.turn.model.PasswordAlgorithm;
import com.jcommsarray.turn.integrity.user.model.UserDetails;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class IntegrityVerificationDetails {

    private final MessageIntegrityAttribute targetAttribute;

    private final UserDetails userDetails;

    private final PasswordAlgorithm passwordAlgorithm;

}
