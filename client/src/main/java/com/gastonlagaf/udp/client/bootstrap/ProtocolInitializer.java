package com.gastonlagaf.udp.client.bootstrap;

import com.gastonlagaf.udp.client.ice.exception.IceFailureException;
import com.gastonlagaf.udp.client.model.ClientProperties;
import com.gastonlagaf.udp.discovery.InternetDiscovery;
import com.gastonlagaf.udp.protocol.ClientProtocol;
import lombok.RequiredArgsConstructor;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.function.Function;

@RequiredArgsConstructor
public class ProtocolInitializer {

    private final Integer minPort;

    private final Integer maxPort;

    public <T extends ClientProtocol<?>> T init(ClientProperties originalProperties, Function<ClientProperties, T> protocolFactory) {
        InetAddress inetAddress = InternetDiscovery.getAddress();
        int port = minPort;
        while (port < maxPort) {
            try {
                InetSocketAddress socketAddress = new InetSocketAddress(inetAddress, port);
                ClientProperties clientProperties = new ClientProperties(
                        socketAddress, originalProperties.getTargetAddress(), originalProperties.getStunAddress(),
                        originalProperties.getTurnAddress(), originalProperties.getSocketTimeout()
                );
                T protocol = protocolFactory.apply(clientProperties);
                protocol.start();
                return protocol;
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            } finally {
                port++;
            }
        }
        throw new IceFailureException("Depleted component ports");
    }

}
