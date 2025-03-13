package com.gastonlagaf.signaling.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.net.InetSocketAddress;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddressCandidate {

    private Integer priority;

    private InetSocketAddress value;

}
