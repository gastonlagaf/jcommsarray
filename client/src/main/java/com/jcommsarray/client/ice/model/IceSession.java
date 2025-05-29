package com.jcommsarray.client.ice.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class IceSession {

    private final IceRole role;

    private final Long tieBreaker;

}
