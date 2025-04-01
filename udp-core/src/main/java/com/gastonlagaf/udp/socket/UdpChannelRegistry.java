package com.gastonlagaf.udp.socket;

import com.gastonlagaf.udp.protocol.Protocol;
import com.gastonlagaf.udp.socket.model.UdpSocketAttachment;
import com.gastonlagaf.udp.socket.model.UdpWriteEntry;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;

@Slf4j
@Getter(AccessLevel.PACKAGE)
public class UdpChannelRegistry implements ChannelRegistry {

    private final List<Selector> selectors;

    private final Integer workers;

    private final AtomicLong connectionsCount = new AtomicLong();

    private final Map<String, BlockingQueue<UdpWriteEntry>> writeQueueMap = new ConcurrentHashMap<>();

    public UdpChannelRegistry(Integer workers) {
        this.workers = workers;
        this.selectors = new ArrayList<>();
        for (int i = 0; i < workers; i++) {
            try {
                Selector selector = Selector.open();
                selectors.add(selector);
            } catch (IOException e) {
                log.error("Failed to register selector", e);
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public SelectionKey register(InetSocketAddress inetSocketAddress, Protocol<?> protocol) {
        String id = UUID.randomUUID().toString();
        return register(id, inetSocketAddress, protocol, null);
    }

    public SelectionKey switchProtocol(SelectionKey selectionKey, Protocol<?> protocol) {
        UdpSocketAttachment attachment = (UdpSocketAttachment) selectionKey.attachment();
        UdpSocketAttachment newAttachment = new UdpSocketAttachment(attachment.getId(), attachment.getSocketAddress(), protocol, attachment.getCloseListener());

        selectionKey.attach(newAttachment);

        return selectionKey;
    }

    @Override
    public void deregister(SelectionKey key) {
        deregister(key, true);
    }

    @Override
    public void close() throws IOException {
        for (Selector selector : selectors) {
            List<SelectableChannel> channels = selector.keys().stream()
                    .map(SelectionKey::channel)
                    .toList();
            selector.close();
            for (SelectableChannel channel : channels) {
                channel.close();
            }
        }
    }

    public SelectionKey register(String id, InetSocketAddress inetSocketAddress, Protocol<?> protocol, BiConsumer<String, InetSocketAddress> closeListener) {
        String queueMapKey = inetSocketAddress.getHostName() + ":" + inetSocketAddress.getPort();
        if (writeQueueMap.containsKey(queueMapKey)) {
            throw new IllegalArgumentException("Address already bound: " + inetSocketAddress);
        }

        UdpSocketAttachment attachment = new UdpSocketAttachment(id, inetSocketAddress, protocol, closeListener);

        DatagramChannel channel = create(inetSocketAddress);

        Selector selector = getSelector();
        SelectionKey selectionKey = registerChannel(channel, selector);
        selectionKey.attach(attachment);

        writeQueueMap.put(queueMapKey, attachment.getWritingQueue());

        log.info("Udp socket registered for interface {} and port {}", inetSocketAddress.getHostName(), inetSocketAddress.getPort());

        return selectionKey;
    }

    private Channel deregister(SelectionKey key, Boolean closeChannel) {
        UdpSocketAttachment attachment = (UdpSocketAttachment) key.attachment();

        InetSocketAddress socketAddress = attachment.getSocketAddress();
        String queueMapKey = socketAddress.getHostName() + ":" + socketAddress.getPort();

        if (!key.isValid()) {
            Optional.ofNullable(attachment.getCloseListener())
                    .ifPresent(it -> it.accept(attachment.getId(), attachment.getSocketAddress()));
            writeQueueMap.remove(queueMapKey);
            return null;
        }

        Channel channel = key.channel();

        key.cancel();
        if (closeChannel) {
            try {
                key.channel().close();
            } catch (IOException e) {
                log.error("Failed to close channel", e);
            }
            Optional.ofNullable(attachment.getCloseListener())
                    .ifPresent(it -> it.accept(attachment.getId(), attachment.getSocketAddress()));
        }
        writeQueueMap.remove(queueMapKey);

        return channel;
    }

    private Selector getSelector() {
        return selectors.stream()
                .min(Comparator.comparing(it -> it.keys().size()))
                .orElseThrow();
    }

    private SelectionKey registerChannel(DatagramChannel channel, Selector selector) {
        try {
            return channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        } catch (ClosedChannelException e) {
            throw new RuntimeException(e);
        }
    }

    private DatagramChannel create(InetSocketAddress inetSocketAddress) {
        DatagramChannel channel;
        try {
            channel = DatagramChannel.open();
            channel.bind(inetSocketAddress);
            channel.configureBlocking(false);
        } catch (IOException e) {
            log.error("Failed to bind to {}", inetSocketAddress, e);
            throw new RuntimeException(e);
        }
        return channel;
    }

}
