package com.gastonlagaf.udp;

import com.gastonlagaf.udp.model.UdpPacketHandlerResult;
import com.gastonlagaf.udp.model.UdpSocketAttachment;
import com.gastonlagaf.udp.model.UdpWriteEntry;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

@Slf4j
public class UdpSockets<T> implements Closeable {

    @Getter
    private final UdpChannelRegistry registry;

    private final ExecutorService workerThreads;

    private final List<WorkerThread> workers;

    public UdpSockets(UdpPacketHandler<T> packetHandler) {
        this(packetHandler, new UdpChannelRegistry(Runtime.getRuntime().availableProcessors()));
    }

    public UdpSockets(UdpPacketHandler<T> packetHandler, Integer workers) {
        this(packetHandler, new UdpChannelRegistry(workers));
    }

    public UdpSockets(UdpPacketHandler<T> packetHandler, UdpChannelRegistry channelRegistry) {
        this.registry = channelRegistry;
        this.workerThreads = Executors.newFixedThreadPool(channelRegistry.getWorkers());
        this.workers = IntStream.range(0, channelRegistry.getWorkers())
                .mapToObj(it -> new WorkerThread(registry.getSelectors().get(it), packetHandler))
                .toList();
    }

    @Override
    public void close() throws IOException {
        registry.close();

        workerThreads.shutdownNow();

        log.info("Sockets has been shut down");
    }

    public void start() {
        this.workers.forEach(workerThreads::submit);
        log.info("Selector started");
    }

    public void send(InetSocketAddress source, InetSocketAddress target, byte[] data) {
        UdpWriteEntry writeEntry = new UdpWriteEntry(target, data);
        String queueMapKey = source.getHostName() + ":" + source.getPort();
        BlockingQueue<UdpWriteEntry> queue = registry.getWriteQueueMap().get(queueMapKey);
        if (null == queue) {
            return;
        }
        try {
            queue.put(writeEntry);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error(e.getMessage());
        }
    }

    @RequiredArgsConstructor
    class WorkerThread implements Runnable {

        private final ByteBuffer buffer = ByteBuffer.allocateDirect(574);

        private final Selector selector;

        private final UdpPacketHandler<T> packetHandler;

        @Override
        public void run() {
            while (selector.isOpen()) {
                Set<SelectionKey> selectedKeys = select();
                Iterator<SelectionKey> iterator = selectedKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    if (!key.isValid()) {
                        registry.deregister(key);
                        continue;
                    }
                    DatagramChannel channel = (DatagramChannel) key.channel();
                    UdpSocketAttachment attachment = (UdpSocketAttachment) key.attachment();
                    if (key.isReadable()) {
                        processRead(channel, attachment);
                    }
                    if (key.isWritable()) {
                        processWrite(key, channel, attachment);
                    }
                }
            }
        }

        private Set<SelectionKey> select() {
            try {
                selector.selectNow();
            } catch (IOException e) {
                log.error("Error selecting sockets", e);
            }
            return selector.selectedKeys();
        }

        private void processRead(DatagramChannel channel, UdpSocketAttachment attachment) {
            try {
                InetSocketAddress senderAddress = (InetSocketAddress) channel.receive(this.buffer);
                if (null == senderAddress) {
                    return;
                }
                this.buffer.flip();
                InetSocketAddress receiverAddress = (InetSocketAddress) channel.socket().getLocalSocketAddress();

                T packet = packetHandler.deserialize(receiverAddress, senderAddress, this.buffer);
                UdpPacketHandlerResult result = packetHandler.handle(receiverAddress, senderAddress, packet);
                if (null != result && !attachment.getScheduleClose()) {
                    result.getTargetAddresses().forEach(it -> send(result.getSenderAddress(), it, result.getData()));

                    if (null != result.getCloseChannel() && result.getCloseChannel()) {
                        attachment.setScheduleClose(true);
                    }
                }
            } catch (Exception e) {
                log.error("Failed to process read", e);
            } finally {
                this.buffer.clear();
            }
        }

        private void processWrite(SelectionKey selectionKey, DatagramChannel channel, UdpSocketAttachment attachment) {
            try {
                UdpWriteEntry writeEntry = attachment.getWritingQueue().poll();
                if (null == writeEntry) {
                    if (attachment.getScheduleClose()) {
                        InetSocketAddress socketAddress = (InetSocketAddress) channel.getLocalAddress();
                        log.info("Deregistered udp socket {}", socketAddress.toString());
                        registry.deregister(selectionKey);
                    }
                    return;
                }
                this.buffer.put(writeEntry.getData());
                this.buffer.flip();
                channel.send(this.buffer, writeEntry.getTarget());
                this.buffer.clear();
            } catch (Exception e) {
                log.error("Failed to process write", e);
            } finally {
                this.buffer.clear();
            }
        }

    }

}
