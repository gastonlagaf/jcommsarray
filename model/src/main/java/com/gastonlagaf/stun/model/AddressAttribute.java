package com.gastonlagaf.stun.model;

import lombok.Getter;
import lombok.SneakyThrows;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

@Getter
public class AddressAttribute extends MessageAttribute {

    private final Boolean xored;

    private final IpFamily ipFamily;

    private final Integer port;

    private final String address;

    public AddressAttribute(Integer type, Integer length, Boolean xored, IpFamily ipFamily, Integer port, String address) {
        super(type, length);
        this.xored = xored;
        this.ipFamily = ipFamily;
        this.port = port;
        this.address = address;
    }

    public AddressAttribute(KnownAttributeName attributeName, SocketAddress socketAddress) {
        super(attributeName.getCode(), 0);

        InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;

        this.xored = KnownAttributeName.XOR_MAPPED_ADDRESS.equals(attributeName);
        this.ipFamily = inetSocketAddress.getAddress() instanceof Inet4Address ? IpFamily.IPV4 : IpFamily.IPV6;
        this.port = inetSocketAddress.getPort();
        this.address = inetSocketAddress.getAddress().getHostAddress();
    }

    @SneakyThrows
    public InetAddress toInetAddress() {
        return InetAddress.getByName(address);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AddressAttribute that)) {
            return false;
        }
        return this.xored.equals(that.xored)
                && ipFamily == that.ipFamily
                && this.port.equals(that.port)
                && this.address.equals(that.address);
    }

}
