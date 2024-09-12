package com.gastonlagaf.stun.client.impl;

import com.gastonlagaf.stun.client.StunClient;
import com.gastonlagaf.stun.client.model.StunClientProperties;
import com.gastonlagaf.stun.codec.MessageCodec;
import com.gastonlagaf.stun.codec.impl.DefaultMessageCodec;
import com.gastonlagaf.stun.exception.StunProtocolException;
import com.gastonlagaf.stun.model.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class UdpStunClient implements StunClient {

    private static final Integer PACKET_LENGTH = 576;

    private final StunClientProperties properties;

    private final DatagramChannel channel;

    private final InetSocketAddress address;

    private final MessageCodec messageCodec = new DefaultMessageCodec();

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @SneakyThrows
    public UdpStunClient(StunClientProperties properties) {
        this.properties = properties;

        this.channel = DatagramChannel.open();

        this.address = new InetSocketAddress(properties.getInterfaceIp(), properties.getClientPort());
        channel.socket().bind(this.address);
        channel.socket().setSoTimeout((int) TimeUnit.SECONDS.toMillis(properties.getSocketTimeout()));

        log.info("Udp socket started at port {}", this.address.getPort());
    }

    @Override
    public Message get() {
        return get(null, Map.of());
    }

    @Override
    public NatBehaviour checkMappingBehaviour() {
        AddressAttribute baseAddressAttribute = new AddressAttribute(KnownAttributeName.XOR_MAPPED_ADDRESS, address);

        Message test1 = get();
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

        Message test1 = get();
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
        MessageAttribute messageAttribute = message.getAttributes().get(KnownAttributeName.XOR_MAPPED_ADDRESS.getCode());
        if (!(messageAttribute instanceof AddressAttribute addressAttribute)) {
            throw new IllegalArgumentException("Mapped address is not present in response");
        }
        return addressAttribute;
    }

    @Override
    @SneakyThrows
    public void close() {
        executorService.shutdown();
        channel.close();
    }

    @SneakyThrows
    private Message get(InetSocketAddress address, Map<Integer, MessageAttribute> attributes) {
        Message message = new Message(attributes);

        ByteBuffer buffer = messageCodec.encode(message);
        buffer.flip();

        SocketAddress socketAddress = Optional.ofNullable(address)
                .orElseGet(() -> new InetSocketAddress(properties.getServerHost(), properties.getServerPort()));
        channel.send(buffer, socketAddress);
        return awaitResponse();
    }

    @SneakyThrows
    private Message awaitResponse() {
        byte[] packetPayload = new byte[PACKET_LENGTH];
        DatagramPacket packet = new DatagramPacket(packetPayload, packetPayload.length);
        try {
            channel.socket().receive(packet);
        } catch (SocketTimeoutException e) {
            return null;
        }
        ByteBuffer byteBuffer = ByteBuffer.wrap(packet.getData());
        Message result = messageCodec.decode(byteBuffer);

        ErrorCodeAttribute errorAttribute = (ErrorCodeAttribute) result.getAttributes().get(KnownAttributeName.ERROR_CODE.getCode());
        if (null != errorAttribute) {
            throw new StunProtocolException(errorAttribute.getReasonPhrase(), errorAttribute.getCode());
        }

        return result;
    }

    private InetSocketAddress getSecondaryServerAddress(Message message) {
        AddressAttribute otherAddress = (AddressAttribute) Optional.ofNullable(message)
                .flatMap(it -> Optional.ofNullable(it.getAttributes().get(KnownAttributeName.OTHER_ADDRESS.getCode())))
                .orElse(null);
        if (null == otherAddress) {
            if (null == properties.getSecondaryServerHost()) {
                throw new IllegalStateException("Secondary server host not exists");
            }
            return new InetSocketAddress(properties.getSecondaryServerHost(), properties.getSecondaryServerPort());
        }
        return new InetSocketAddress(otherAddress.getAddress(), otherAddress.getPort());
    }

}
