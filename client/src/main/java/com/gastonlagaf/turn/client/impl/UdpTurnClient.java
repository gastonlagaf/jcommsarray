package com.gastonlagaf.turn.client.impl;

import com.gastonlagaf.handler.MessageHandler;
import com.gastonlagaf.udp.codec.CommunicationCodec;
import com.gastonlagaf.stun.exception.StunProtocolException;
import com.gastonlagaf.stun.model.*;
import com.gastonlagaf.udp.UdpSockets;
import com.gastonlagaf.turn.client.TurnClient;
import com.gastonlagaf.udp.BaseUdpClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.util.*;
import java.util.concurrent.*;

public class UdpTurnClient extends BaseUdpClient<Message> implements TurnClient {

    private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r);
        thread.setDaemon(true);
        return thread;
    });

    private static final Integer DEFAULT_LIFETIME_MINUTES = 10;

    private static final Long REFRESH_RATE_MINUTES = DEFAULT_LIFETIME_MINUTES - 1L;

    private final InetSocketAddress sourceAddress;

    private final InetSocketAddress targetAddress;

    private final MessageHandler messageHandler;

    private final Map<Integer, InetSocketAddress> channelBindings = new HashMap<>();

    private final Set<InetSocketAddress> bindings = new HashSet<>();

    private final ScheduledFuture<?> refreshSchedule;

    private final DatagramChannel channel;

    public UdpTurnClient(InetSocketAddress sourceAddress, InetSocketAddress targetAddress, UdpSockets<Message> udpSockets, CommunicationCodec<Message> communicationCodec, DatagramChannel channel, MessageHandler messageHandler) {
        super(udpSockets, communicationCodec);
        this.sourceAddress = sourceAddress;
        this.targetAddress = targetAddress;
        this.messageHandler = messageHandler;
        this.channel = channel;

        this.refreshSchedule = executor.scheduleAtFixedRate(
                () -> refresh(DEFAULT_LIFETIME_MINUTES),
                REFRESH_RATE_MINUTES, REFRESH_RATE_MINUTES, TimeUnit.MINUTES
        );
    }

    @Override
    public CompletableFuture<Message> registerAwait(Message message) {
        return messageHandler.awaitResult(message.getHeader().getTransactionId());
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
        int channelNumber;
        do {
            channelNumber = new Random().nextInt(
                    ChannelNumberAttribute.MIN_CHANNEL_NUMBER, ChannelNumberAttribute.MAX_CHANNEL_NUMBER + 1
            );
        } while (channelBindings.containsKey(channelNumber));

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
    public void send(Integer channelNumber, byte[] data) {
        if (!channelBindings.containsKey(channelNumber)) {
            throw new StunProtocolException("Binding not registered for channel " + channelNumber, ErrorCode.BAD_REQUEST.getCode());
        }
        MessageHeader messageHeader = new MessageHeader(MessageType.INBOUND_CHANNEL_DATA);
        Map<Integer, MessageAttribute> attributes = Map.of(
                KnownAttributeName.CHANNEL_NUMBER.getCode(), new ChannelNumberAttribute(channelNumber),
                KnownAttributeName.DATA.getCode(), new DefaultMessageAttribute(KnownAttributeName.DATA.getCode(), data.length, data)
        );

        Message message = new Message(messageHeader, attributes);
        send(this.sourceAddress, this.targetAddress, message);
    }

    @Override
    public void send(InetSocketAddress receiver, byte[] data) {
        if (!bindings.contains(receiver)) {
            throw new StunProtocolException("Binding not registered for address " + receiver.toString(), ErrorCode.BAD_REQUEST.getCode());
        }
        MessageHeader messageHeader = new MessageHeader(MessageType.SEND);
        Map<Integer, MessageAttribute> attributes = Map.of(
                KnownAttributeName.XOR_PEER_ADDRESS.getCode(), new AddressAttribute(KnownAttributeName.XOR_PEER_ADDRESS, receiver),
                KnownAttributeName.DATA.getCode(), new DefaultMessageAttribute(KnownAttributeName.DATA.getCode(), data.length, data)
        );

        Message message = new Message(messageHeader, attributes);
        send(this.sourceAddress, this.targetAddress, message);
    }

    @Override
    public void close() {
        refresh(LifetimeAttribute.DELETE_ALLOCATION_LIFETIME_MARK);
        refreshSchedule.cancel(true);
        try {
            channel.close();
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
        Message response = sendAndReceive(this.sourceAddress, this.targetAddress, message);
        ErrorCodeAttribute errorAttribute = response.getAttributes().get(KnownAttributeName.ERROR_CODE);
        if (null != errorAttribute) {
            throw new StunProtocolException(errorAttribute.getReasonPhrase(), errorAttribute.getCode());
        }
    }

}
