package com.jcommsarray.test.socket;

import com.jcommsarray.test.protocol.Protocol;
import com.jcommsarray.test.protocol.model.UdpPacketHandlerResult;
import com.jcommsarray.test.socket.model.UdpSocketAttachment;
import com.jcommsarray.test.socket.model.UdpWriteEntry;
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
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

@Slf4j
public class UdpSockets implements Closeable {

    @Getter
    private final UdpChannelRegistry registry;

    private final ExecutorService workerThreads;

    private final List<WorkerThread> workers;

    public UdpSockets(Integer workers) {
        this(new UdpChannelRegistry(workers));
    }

    public UdpSockets(UdpChannelRegistry channelRegistry) {
        this.registry = channelRegistry;
        this.workerThreads = Executors.newFixedThreadPool(channelRegistry.getWorkers());
        this.workers = IntStream.range(0, channelRegistry.getWorkers())
                .mapToObj(it -> new WorkerThread(registry.getSelectors().get(it)))
                .toList();
    }

    @Override
    public void close() throws IOException {
        workers.forEach(WorkerThread::close);
        workerThreads.shutdownNow();

        registry.close();

        log.info("Sockets has been shut down");
    }

    public void start() {
        if (workerThreads.isShutdown()) {
            throw new IllegalArgumentException("Cannot start again");
        }
        this.workers.forEach(workerThreads::submit);
        log.info("Selector started");
    }

    public CompletableFuture<Void> send(InetSocketAddress source, InetSocketAddress target, ByteBuffer data) {
        log.info("Sending data from {} to {}", source, target);
        String queueMapKey = source.getHostName() + ":" + source.getPort();
        BlockingQueue<UdpWriteEntry> queue = registry.getWriteQueueMap().get(queueMapKey);
        if (null == queue) {
            log.info("Skipping send");
            return CompletableFuture.completedFuture(null);
        }
        try {
            CompletableFuture<Void> future = new CompletableFuture<>();
            UdpWriteEntry writeEntry = new UdpWriteEntry(target, data, future);
            queue.put(writeEntry);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error(e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
        return CompletableFuture.completedFuture(null);
    }

    @RequiredArgsConstructor
    class WorkerThread implements Runnable, Closeable {

        private final ByteBuffer buffer = ByteBuffer.allocateDirect(574);

        private final Selector selector;

        private final AtomicBoolean running = new AtomicBoolean(true);

        @Override
        public void run() {
            while (selector.isOpen() && running.get()) {
                try {
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
                } catch (Exception e) {
                    log.error("", e.getMessage());
                }
            }
        }

        @Override
        public void close() {
            running.set(false);
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

                Protocol<?> protocol = attachment.getProtocol();
                UdpPacketHandlerResult result = protocol.handle(receiverAddress, senderAddress, this.buffer);
                if (null != result && !attachment.getScheduleClose()) {
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
            UdpWriteEntry writeEntry = attachment.getWritingQueue().poll();
            try {
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
                writeEntry.getFuture().complete(null);
            } catch (Exception e) {
                log.error("Failed to process write", e);
                Optional.ofNullable(writeEntry).ifPresent(it -> it.getFuture().completeExceptionally(e));
            } finally {
                this.buffer.clear();
            }
        }

    }

}
