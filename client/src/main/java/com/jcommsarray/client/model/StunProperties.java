package com.jcommsarray.client.model;

import com.jcommsarray.turn.integrity.user.model.UserDetails;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.net.InetSocketAddress;

@Getter
@RequiredArgsConstructor
public class StunProperties {

    private final InetSocketAddress address;

    private final UserDetails userDetails;

}
