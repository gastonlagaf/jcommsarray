package com.gastonlagaf.udp.turn.integrity.model;

import com.gastonlagaf.udp.turn.model.PasswordAlgorithm;
import com.gastonlagaf.udp.turn.user.model.UserDetails;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class IntegrityDetails {

    private final UserDetails userDetails;

    private final PasswordAlgorithm passwordAlgorithm;

}
