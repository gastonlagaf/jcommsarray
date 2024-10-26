package com.gastonlagaf.udp;

import com.gastonlagaf.udp.model.UdpSocketAttachment;
import com.gastonlagaf.udp.model.UdpWriteEntry;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
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

    public SelectionKey register(String interfaceIp, Integer port) {
        String id = UUID.randomUUID().toString();
        return register(id, interfaceIp, port, null);
    }

    public SelectionKey register(String id, String interfaceIp, Integer port, BiConsumer<String, InetSocketAddress> closeListener) {
        InetSocketAddress inetSocketAddress = new InetSocketAddress(interfaceIp, port);
        DatagramChannel channel = create(inetSocketAddress);

        UdpSocketAttachment attachment = new UdpSocketAttachment(id, inetSocketAddress, closeListener);
        String queueMapKey = inetSocketAddress.getHostName() + ":" + inetSocketAddress.getPort();
        writeQueueMap.put(queueMapKey, attachment.getWritingQueue());

        Selector selector = selectors.stream()
                .min(Comparator.comparing(it -> it.keys().size()))
                .orElseThrow();
        SelectionKey selectionKey = registerChannel(channel, selector);
        selectionKey.attach(attachment);

        log.info("Udp socket registered for interface {} and port {}", interfaceIp, port);

        return selectionKey;
    }

    @Override
    public void deregister(SelectionKey key) {
        UdpSocketAttachment attachment = (UdpSocketAttachment) key.attachment();
        if (!key.isValid()) {
            attachment.getCloseListener().accept(attachment.getId(), attachment.getSocketAddress());
            return;
        }
        key.cancel();
        try {
            key.channel().close();
        } catch (IOException e) {
            log.error("Failed to close channel", e);
        }
        attachment.getCloseListener().accept(attachment.getId(), attachment.getSocketAddress());
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
