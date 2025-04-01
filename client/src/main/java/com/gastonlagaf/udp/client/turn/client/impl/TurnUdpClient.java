package com.gastonlagaf.udp.client.turn.client.impl;

import com.gastonlagaf.udp.client.UdpClient;
import com.gastonlagaf.udp.client.UdpClientDelegate;
import com.gastonlagaf.udp.client.turn.client.TurnClient;
import com.gastonlagaf.udp.turn.exception.StunProtocolException;
import com.gastonlagaf.udp.turn.model.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TurnUdpClient extends UdpClientDelegate<Message> implements TurnClient {

    private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r);
        thread.setDaemon(true);
        return thread;
    });

    private static final Integer DEFAULT_LIFETIME_MINUTES = 10;

    private static final Long REFRESH_RATE_MINUTES = DEFAULT_LIFETIME_MINUTES - 1L;

    private final Map<Integer, InetSocketAddress> channelBindings;

    private final Set<InetSocketAddress> bindings = new HashSet<>();

    private final ScheduledFuture<?> refreshSchedule;

    private final InetSocketAddress sourceAddress;

    private final AtomicInteger channelCounter = new AtomicInteger(ChannelNumberAttribute.MIN_CHANNEL_NUMBER);

    private InetSocketAddress targetAddress = null;

    public TurnUdpClient(UdpClient<Message> udpClient, InetSocketAddress sourceAddress, Map<Integer, InetSocketAddress> channelBindings) {
        super(udpClient);
        this.sourceAddress = sourceAddress;
        this.channelBindings = channelBindings;
        this.refreshSchedule = executor.scheduleAtFixedRate(
                () -> refresh(DEFAULT_LIFETIME_MINUTES),
                REFRESH_RATE_MINUTES, REFRESH_RATE_MINUTES, TimeUnit.MINUTES
        );
    }

    @Override
    public void createPermission(List<InetSocketAddress> targets) {
        MessageHeader messageHeader = new MessageHeader(MessageType.CREATE_PERMISSION);
        List<MessageAttribute> peersList = targets.stream()
                .<MessageAttribute>map(it -> new AddressAttribute(KnownAttributeName.XOR_PEER_ADDRESS, it))
                .toList();
        Map<Integer, List<MessageAttribute>> attributes = Map.of(
                KnownAttributeName.XOR_PEER_ADDRESS.getCode(), peersList
        );
        MessageAttributes messageAttributes = new MessageAttributes(attributes);

        Message message = new Message(messageHeader, messageAttributes);
        sendAndReceive(message);

        bindings.addAll(targets);
    }

    @Override
    public Integer createChannel(InetSocketAddress target) {
        int channelNumber = channelCounter.getAndIncrement();

        return createChannel(channelNumber, target);
    }

    @Override
    public Integer createChannel(Integer number, InetSocketAddress target) {
        MessageHeader messageHeader = new MessageHeader(MessageType.CHANNEL_BIND);
        Map<Integer, MessageAttribute> attributes = Map.of(
                KnownAttributeName.CHANNEL_NUMBER.getCode(), new ChannelNumberAttribute(number),
                KnownAttributeName.XOR_PEER_ADDRESS.getCode(), new AddressAttribute(KnownAttributeName.XOR_PEER_ADDRESS, target)
        );
        Message message = new Message(messageHeader, attributes);
        sendAndReceive(message);
        channelBindings.put(number, target);
        bindings.add(target);

        return number;
    }

    @Override
    public CompletableFuture<Void> send(Integer channelNumber, byte[] data) {
        assertTurnSessionStarted();
        if (!channelBindings.containsKey(channelNumber)) {
            throw new StunProtocolException("Binding not registered for channel " + channelNumber, ErrorCode.BAD_REQUEST.getCode());
        }
        MessageHeader messageHeader = new MessageHeader(MessageType.INBOUND_CHANNEL_DATA);
        Map<Integer, MessageAttribute> attributes = Map.of(
                KnownAttributeName.CHANNEL_NUMBER.getCode(), new ChannelNumberAttribute(channelNumber),
                KnownAttributeName.DATA.getCode(), new DefaultMessageAttribute(KnownAttributeName.DATA.getCode(), data.length, data)
        );

        Message message = new Message(messageHeader, attributes);
        return send(this.targetAddress, message);
    }

    @Override
    public CompletableFuture<Void> send(InetSocketAddress receiver, byte[] data) {
        assertTurnSessionStarted();
        if (!bindings.contains(receiver)) {
            throw new StunProtocolException("Binding not registered for address " + receiver.toString(), ErrorCode.BAD_REQUEST.getCode());
        }
        MessageHeader messageHeader = new MessageHeader(MessageType.SEND);
        Map<Integer, MessageAttribute> attributes = Map.of(
                KnownAttributeName.XOR_PEER_ADDRESS.getCode(), new AddressAttribute(KnownAttributeName.XOR_PEER_ADDRESS, receiver),
                KnownAttributeName.DATA.getCode(), new DefaultMessageAttribute(KnownAttributeName.DATA.getCode(), data.length, data)
        );

        Message message = new Message(messageHeader, attributes);
        return send(this.sourceAddress, this.targetAddress, message);
    }

    public InetSocketAddress start(InetSocketAddress turnAddress) {
        MessageHeader messageHeader = new MessageHeader(MessageType.ALLOCATE);
        Map<Integer, MessageAttribute> attributes = Map.of(
                KnownAttributeName.REQUESTED_TRANSPORT.getCode(), new RequestedTransportAttribute(Protocol.UDP)
        );
        Message message = new Message(messageHeader, attributes);
        Message response = sendAndReceive(turnAddress, message).join();

        this.targetAddress = response.getAttributes()
                .<AddressAttribute>get(KnownAttributeName.XOR_RELAYED_ADDRESS)
                .toInetSocketAddress();
        return this.targetAddress;
    }

    @Override
    public InetSocketAddress getProxyAddress() {
        return this.targetAddress;
    }

    @Override
    public void close() {
        refresh(LifetimeAttribute.DELETE_ALLOCATION_LIFETIME_MARK);
        refreshSchedule.cancel(true);
        try {
            super.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void refresh(Integer timeMinutes) {
        Integer lifetime = Optional.ofNullable(timeMinutes).orElse(DEFAULT_LIFETIME_MINUTES);
        MessageHeader messageHeader = new MessageHeader(MessageType.REFRESH);
        Map<Integer, MessageAttribute> attributes = Map.of(
                KnownAttributeName.LIFETIME.getCode(), new LifetimeAttribute(lifetime)
        );
        Message message = new Message(messageHeader, attributes);
        sendAndReceive(message);
    }

    private void sendAndReceive(Message message) {
        assertTurnSessionStarted();
        Message response = sendAndReceive(this.targetAddress, message).join();
        ErrorCodeAttribute errorAttribute = response.getAttributes().get(KnownAttributeName.ERROR_CODE);
        if (null != errorAttribute) {
            throw new StunProtocolException(errorAttribute.getReasonPhrase(), errorAttribute.getCode());
        }
    }

    private void assertTurnSessionStarted() {
        if (null == targetAddress) {
            throw new IllegalStateException("Turn session has not been started");
        }
    }

}
