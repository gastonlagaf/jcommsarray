package com.gastonlagaf.stun.integrity.model;

import com.gastonlagaf.stun.model.MessageIntegrityAttribute;
import com.gastonlagaf.stun.model.PasswordAlgorithm;
import com.gastonlagaf.stun.user.model.UserDetails;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class IntegrityVerificationDetails {

    private final MessageIntegrityAttribute targetAttribute;

    private final UserDetails userDetails;

    private final PasswordAlgorithm passwordAlgorithm;

}
