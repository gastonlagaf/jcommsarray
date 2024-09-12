package com.gastonlagaf.stun.server.impl;

import com.gastonlagaf.stun.codec.MessageCodec;
import com.gastonlagaf.stun.codec.impl.DefaultMessageCodec;
import com.gastonlagaf.stun.model.Message;
import com.gastonlagaf.stun.server.StunServer;
import com.gastonlagaf.stun.server.handler.StunMessageHandler;
import com.gastonlagaf.stun.server.handler.impl.DefaultStunMessageHandler;
import com.gastonlagaf.stun.server.model.ServerType;
import com.gastonlagaf.stun.server.model.StunRequest;
import com.gastonlagaf.stun.server.model.StunResponse;
import com.gastonlagaf.stun.server.model.StunServerProperties;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class UdpStunServer implements StunServer {

    private static final Integer PACKET_LENGTH = 576;

    private final StunServerProperties properties;

    private final Map<ServerType, DatagramChannel> channelMap;

    private final StunMessageHandler handler;

    private final ExecutorService executorService;

    private final MessageCodec messageCodec = new DefaultMessageCodec();

    @SneakyThrows
    public UdpStunServer(StunServerProperties properties) {
        this.properties = properties;
        this.channelMap = new HashMap<>();

        this.channelMap.put(ServerType.PRIMARY, DatagramChannel.open());
        if (null != properties.getPrimaryInstance().getAlternatePort()) {
            this.channelMap.put(ServerType.PRIMARY_ALTERNATIVE, DatagramChannel.open());
        }
        if (null != properties.getSecondaryInstance()) {
            this.channelMap.put(ServerType.SECONDARY, DatagramChannel.open());
            if (null != properties.getSecondaryInstance().getAlternatePort()) {
                this.channelMap.put(ServerType.SECONDARY_ALTERNATIVE, DatagramChannel.open());
            }
        }

        this.handler = new DefaultStunMessageHandler(this.channelMap);

        this.executorService = Executors.newFixedThreadPool(this.channelMap.size());
    }

    @Override
    @SneakyThrows
    public void start() {
        channelMap.forEach((serverType, channel) -> launch(serverType, channel, serverType.getSocketAddress(properties)));
    }

    @Override
    @SneakyThrows
    public void stop() {
        executorService.shutdownNow();

        for (DatagramChannel channel : channelMap.values()) {
            channel.close();
        }

        log.info("Stun server shutdown completed");
    }

    @SneakyThrows
    private void launch(ServerType serverType, DatagramChannel channel, InetSocketAddress address) {
        channel.socket().bind(address);

        executorService.submit(() -> {
            while (true) {
                try {
                    ByteBuffer byteBuffer = ByteBuffer.allocate(PACKET_LENGTH);
                    SocketAddress socketAddress = channel.receive(byteBuffer);
                    process(serverType, channel, socketAddress, byteBuffer);
                } catch (Exception e) {
                    log.error("Error handling stun message", e);
                }
            }
        });

        log.info("Started UDP stun server on port: {}", address.getPort());
    }

    @SneakyThrows
    private void process(ServerType serverType, DatagramChannel channel, SocketAddress socketAddress, ByteBuffer buffer) {
        buffer.flip();
        Message message = messageCodec.decode(buffer);
        StunRequest stunRequest = new StunRequest(socketAddress, message);
        StunResponse response = handler.handle(serverType, channel, stunRequest);
        ByteBuffer responseBuffer = messageCodec.encode(response.getMessage());
        responseBuffer.flip();
        response.getSenderChannel().send(responseBuffer, response.getReceiverAddress());
    }

}
