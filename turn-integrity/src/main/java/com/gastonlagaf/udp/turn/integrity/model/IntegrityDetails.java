package com.gastonlagaf.udp.client.stun.integrity.model;

import com.gastonlagaf.udp.client.stun.model.PasswordAlgorithm;
import com.gastonlagaf.udp.client.stun.user.model.UserDetails;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class IntegrityDetails {

    private final UserDetails userDetails;

    private final PasswordAlgorithm passwordAlgorithm;

}
