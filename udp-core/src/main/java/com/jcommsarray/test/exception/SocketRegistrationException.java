package com.jcommsarray.test.exception;

import java.net.InetSocketAddress;

public class SocketRegistrationException extends RuntimeException {

    public SocketRegistrationException(InetSocketAddress inetSocketAddress) {
        super("Socket with such address is already bound: " + inetSocketAddress);
    }

}
