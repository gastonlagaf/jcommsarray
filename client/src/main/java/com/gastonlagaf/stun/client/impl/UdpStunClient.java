package com.gastonlagaf.stun.client.impl;

import com.gastonlagaf.handler.MessageHandler;
import com.gastonlagaf.stun.client.StunClient;
import com.gastonlagaf.stun.client.model.StunClientProperties;
import com.gastonlagaf.stun.codec.impl.MessageCodec;
import com.gastonlagaf.stun.exception.StunProtocolException;
import com.gastonlagaf.stun.model.*;
import com.gastonlagaf.udp.UdpSockets;
import com.gastonlagaf.turn.client.TurnClient;
import com.gastonlagaf.turn.client.impl.UdpTurnClient;
import com.gastonlagaf.udp.BaseUdpClient;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class UdpStunClient extends BaseUdpClient<Message> implements StunClient {

    private final StunClientProperties properties;

    private final MessageHandler messageHandler;

    private final InetSocketAddress address;

    private final InetSocketAddress defaultTargetAddress;

    public UdpStunClient(StunClientProperties properties) {
        this(
                properties,
                new MessageHandler(properties.getSocketTimeout().longValue(), null)
        );
    }

    public UdpStunClient(StunClientProperties properties, MessageHandler messageHandler) {
        super(
                new UdpSockets<>(messageHandler, 1),
                new MessageCodec()
        );
        this.properties = properties;
        this.messageHandler = messageHandler;

        DatagramSocket socket = ((DatagramChannel) udpSockets.getRegistry()
                .register(properties.getInterfaceIp(), properties.getClientPort())
                .channel())
                .socket();
        this.address = (InetSocketAddress) socket.getLocalSocketAddress();

        this.defaultTargetAddress = new InetSocketAddress(properties.getServerHost(), properties.getServerPort());

        udpSockets.start();
    }

    @Override
    public CompletableFuture<Message> registerAwait(Message message) {
        return messageHandler.awaitResult(message.getHeader().getTransactionId());
    }

    @Override
    public TurnClient initializeTurnSession() {
        int sourcePort = new Random().nextInt(45000, 65000);
        InetSocketAddress sourceAddress = new InetSocketAddress(this.address.getHostName(), sourcePort);
        DatagramChannel channel = (DatagramChannel) udpSockets.getRegistry()
                .register(sourceAddress.getHostName(), sourceAddress.getPort())
                .channel();

        Message message = doInitTurnSession(sourceAddress);

        InetSocketAddress targetAddress = message.getAttributes()
                .<AddressAttribute>get(KnownAttributeName.XOR_RELAYED_ADDRESS)
                .toInetSocketAddress();
        
        return new UdpTurnClient(sourceAddress, targetAddress, udpSockets, communicationCodec, channel, messageHandler);
    }

    @Override
    public InetSocketAddress getReflexiveAddress() {
        Message message = get(null, Map.of());
        AddressAttribute addressAttribute = message.getAttributes().get(KnownAttributeName.XOR_MAPPED_ADDRESS);
        if (null == addressAttribute) {
            throw new StunProtocolException("Mapped address not found in response", ErrorCode.BAD_REQUEST.getCode());
        }
        return new InetSocketAddress(addressAttribute.getAddress(), addressAttribute.getPort());
    }

    @Override
    public NatBehaviour checkMappingBehaviour() {
        AddressAttribute baseAddressAttribute = new AddressAttribute(KnownAttributeName.XOR_MAPPED_ADDRESS, address);

        Message test1 = get(null, Map.of());
        AddressAttribute addressAttribute1 = getMappedAddress(test1);
        if (addressAttribute1.equals(baseAddressAttribute)) {
            return NatBehaviour.NO_NAT;
        }

        InetSocketAddress address2 = getSecondaryServerAddress(test1);
        Message test2 = get(address2, Map.of());
        AddressAttribute addressAttribute2 = getMappedAddress(test2);
        if (addressAttribute2.equals(addressAttribute1)) {
            return NatBehaviour.ENDPOINT_INDEPENDENT;
        }

        InetSocketAddress address3 = new InetSocketAddress(addressAttribute2.getAddress(), properties.getServerPort());
        Message test3 = get(address3, Map.of());
        AddressAttribute addressAttribute3 = getMappedAddress(test3);

        return addressAttribute3.equals(addressAttribute2) ? NatBehaviour.ADDRESS_DEPENDENT : NatBehaviour.ADDRESS_AND_PORT_DEPENDENT;
    }

    @Override
    public NatBehaviour checkFilteringBehaviour() {
        AddressAttribute baseAddressAttribute = new AddressAttribute(KnownAttributeName.XOR_MAPPED_ADDRESS, address);

        Message test1 = get(null, Map.of());
        AddressAttribute addressAttribute1 = getMappedAddress(test1);
        if (addressAttribute1.equals(baseAddressAttribute)) {
            return NatBehaviour.NO_NAT;
        }

        Message test2 = get(null, Map.of(
                KnownAttributeName.CHANGE_REQUEST.getCode(), new ChangeRequestAttribute(false, true)
        ));
        if (null != test2) {
            return NatBehaviour.ENDPOINT_INDEPENDENT;
        }

        Message test3 = get(null, Map.of(
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
    public Message sendAndReceive(InetSocketAddress source, InetSocketAddress target, Message message) {
        Message response = super.sendAndReceive(source, target, message);
        ErrorCodeAttribute errorAttribute = response.getAttributes().get(KnownAttributeName.ERROR_CODE);
        if (null != errorAttribute) {
            throw new StunProtocolException(errorAttribute.getReasonPhrase(), errorAttribute.getCode());
        }
        return response;
    }

    @Override
    public void close() {
        try {
            udpSockets.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Message get(InetSocketAddress address, Map<Integer, MessageAttribute> attributes) {
        Message message = new Message(attributes);

        InetSocketAddress targetAddress = Optional.ofNullable(address).orElse(this.defaultTargetAddress);

        return sendAndReceive(this.address, targetAddress, message);
    }

    private Message doInitTurnSession(InetSocketAddress sourceAddress) {
        MessageHeader messageHeader = new MessageHeader(MessageType.ALLOCATE);
        Map<Integer, MessageAttribute> attributes = Map.of(
                KnownAttributeName.REQUESTED_TRANSPORT.getCode(), new RequestedTransportAttribute(Protocol.UDP)
        );
        Message message = new Message(messageHeader, attributes);
        return sendAndReceive(sourceAddress, this.defaultTargetAddress, message);
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
