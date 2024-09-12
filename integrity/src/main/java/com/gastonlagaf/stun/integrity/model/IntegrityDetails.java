package com.gastonlagaf.stun.integrity.model;

import com.gastonlagaf.stun.model.PasswordAlgorithm;
import com.gastonlagaf.stun.user.model.UserDetails;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class IntegrityDetails {

    private final UserDetails userDetails;

    private final PasswordAlgorithm passwordAlgorithm;

}
