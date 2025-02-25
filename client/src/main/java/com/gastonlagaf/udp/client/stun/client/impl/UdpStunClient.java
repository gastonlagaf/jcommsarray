package com.gastonlagaf.udp.client.stun.client.impl;

import com.gastonlagaf.udp.client.stun.StunClientProtocol;
import com.gastonlagaf.udp.client.stun.client.StunClient;
import com.gastonlagaf.udp.client.model.ClientProperties;
import com.gastonlagaf.udp.client.stun.exception.StunProtocolException;
import com.gastonlagaf.udp.client.stun.model.*;
import com.gastonlagaf.udp.client.BaseUdpClient;
import com.gastonlagaf.udp.socket.UdpSockets;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class UdpStunClient extends BaseUdpClient<Message> implements StunClient {

    private final ClientProperties properties;

    private final InetSocketAddress defaultTargetAddress;

    public UdpStunClient(ClientProperties properties, UdpSockets<Message> udpSockets, StunClientProtocol protocol) {
        super(
                udpSockets, protocol, properties.getHostAddress()
        );
        this.properties = properties;
        this.defaultTargetAddress = properties.getStunAddress();
    }

    @Override
    public InetSocketAddress getReflexiveAddress() {
        Message message = sendBinding(null, Map.of());
        AddressAttribute addressAttribute = message.getAttributes().get(KnownAttributeName.XOR_MAPPED_ADDRESS);
        if (null == addressAttribute) {
            throw new StunProtocolException("Mapped address not found in response", ErrorCode.BAD_REQUEST.getCode());
        }
        return new InetSocketAddress(addressAttribute.getAddress(), addressAttribute.getPort());
    }

    @Override
    public NatBehaviour checkMappingBehaviour() {
        AddressAttribute baseAddressAttribute = new AddressAttribute(KnownAttributeName.XOR_MAPPED_ADDRESS, this.sourceAddress);

        Message test1 = sendBinding(null, Map.of());
        AddressAttribute addressAttribute1 = getMappedAddress(test1);
        if (addressAttribute1.equals(baseAddressAttribute)) {
            return NatBehaviour.NO_NAT;
        }

        InetSocketAddress address2 = getSecondaryServerAddress(test1);
        Message test2 = sendBinding(address2, Map.of());
        AddressAttribute addressAttribute2 = getMappedAddress(test2);
        if (addressAttribute2.equals(addressAttribute1)) {
            return NatBehaviour.ENDPOINT_INDEPENDENT;
        }

        InetSocketAddress address3 = new InetSocketAddress(addressAttribute2.getAddress(), properties.getTargetPort());
        Message test3 = sendBinding(address3, Map.of());
        AddressAttribute addressAttribute3 = getMappedAddress(test3);

        return addressAttribute3.equals(addressAttribute2) ? NatBehaviour.ADDRESS_DEPENDENT : NatBehaviour.ADDRESS_AND_PORT_DEPENDENT;
    }

    @Override
    public NatBehaviour checkFilteringBehaviour() {
        AddressAttribute baseAddressAttribute = new AddressAttribute(KnownAttributeName.XOR_MAPPED_ADDRESS, this.sourceAddress);

        Message test1 = sendBinding(null, Map.of());
        AddressAttribute addressAttribute1 = getMappedAddress(test1);
        if (addressAttribute1.equals(baseAddressAttribute)) {
            return NatBehaviour.NO_NAT;
        }

        Message test2 = sendBinding(null, Map.of(
                KnownAttributeName.CHANGE_REQUEST.getCode(), new ChangeRequestAttribute(false, true)
        ));
        if (null != test2) {
            return NatBehaviour.ENDPOINT_INDEPENDENT;
        }

        Message test3 = sendBinding(null, Map.of(
                KnownAttributeName.CHANGE_REQUEST.getCode(), new ChangeRequestAttribute(false, true)
        ));
        return null != test3 ? NatBehaviour.ADDRESS_DEPENDENT : NatBehaviour.ADDRESS_AND_PORT_DEPENDENT;
    }

    private AddressAttribute getMappedAddress(Message message) {
        if (null == message) {
            throw new IllegalStateException("Message cannot be null");
        }
        MessageAttribute messageAttribute = message.getAttributes().get(KnownAttributeName.XOR_MAPPED_ADDRESS);
        if (!(messageAttribute instanceof AddressAttribute addressAttribute)) {
            throw new IllegalArgumentException("Mapped address is not present in response");
        }
        return addressAttribute;
    }

    @Override
    public CompletableFuture<Message> sendAndReceive(InetSocketAddress source, InetSocketAddress target, Message message) {
        return super.sendAndReceive(source, target, message)
                .thenApply(it -> {
                    ErrorCodeAttribute errorAttribute = it.getAttributes().get(KnownAttributeName.ERROR_CODE);
                    if (null != errorAttribute) {
                        throw new StunProtocolException(errorAttribute.getReasonPhrase(), errorAttribute.getCode());
                    }
                    return it;
                });
    }

    @Override
    public void close() {
        try {
            udpSockets.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Message sendBinding(InetSocketAddress address, Map<Integer, MessageAttribute> attributes) {
        Message message = new Message(attributes);

        InetSocketAddress targetAddress = Optional.ofNullable(address).orElse(this.defaultTargetAddress);

        return sendAndReceive(targetAddress, message).join();
    }

    private InetSocketAddress getSecondaryServerAddress(Message message) {
        AddressAttribute otherAddress = Optional.ofNullable(message)
                .flatMap(it -> Optional.ofNullable(it.getAttributes().<AddressAttribute>get(KnownAttributeName.OTHER_ADDRESS)))
                .orElse(null);
        if (null == otherAddress) {
            throw new IllegalStateException("Secondary server host not exists");
        }
        return new InetSocketAddress(otherAddress.getAddress(), otherAddress.getPort());
    }

}
