package com.gastonlagaf.stun.server.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class StunServerProperties {

    private final StunInstanceProperties primaryInstance;

    private final StunInstanceProperties secondaryInstance;

}
