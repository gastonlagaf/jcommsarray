package com.jcommsarray.signaling.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    private String type;

    private String address;

    private Integer port;

    public AddressCandidate(Integer priority, String type, InetSocketAddress value) {
        this.priority = priority;
        this.type = type;
        this.address = value.getAddress().getHostAddress();
        this.port = value.getPort();
    }

    @JsonIgnore
    public InetSocketAddress getValue() {
        return new InetSocketAddress(address, port);
    }

}
