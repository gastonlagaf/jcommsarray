package com.gastonlagaf.stun.client;

import com.gastonlagaf.stun.client.impl.UdpStunClient;
import com.gastonlagaf.stun.client.model.StunClientProperties;
import com.gastonlagaf.stun.model.NatBehaviour;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class ClientBootstrap {

    @SneakyThrows
    public static void main(String[] args) {
        String interfaceHost = NetworkInterface.getByName("en0").inetAddresses()
                .filter(it -> it instanceof Inet4Address)
                .findFirst()
                .orElseThrow()
                .getHostName();
        Integer port = 45199;

        getMappedAddress(interfaceHost, port);
//        communicate(interfaceHost, port, "37.45.144.9", 45188);
    }

    private static void getMappedAddress(String interfaceIp, Integer port) {
        StunClientProperties properties = new StunClientProperties(interfaceIp, port, "stun.antisip.com", 3478, null, null, 5);
        try (StunClient stunClient = new UdpStunClient(properties)) {
            NatBehaviour behaviour = stunClient.checkFilteringBehaviour();
            System.out.println(behaviour);
//            AddressAttribute mappedAddress = (AddressAttribute) message.getAttributes().get(KnownAttributeName.XOR_MAPPED_ADDRESS.getCode());
//            log.info("Got address {}:{}", mappedAddress.getAddress(), mappedAddress.getPort());
        } catch (Exception e) {
            log.error("Error getting mapped address", e);
        }
    }

    @SneakyThrows
    private static void communicate(String interfaceIp, Integer port, String targetHost, int targetPort) {
        DatagramChannel socket = DatagramChannel.open();
        socket.socket().bind(new InetSocketAddress(interfaceIp, port));

        CompletableFuture.runAsync(() -> {
            while (socket.isOpen()) {
                try {
                    ByteBuffer byteBuffer = ByteBuffer.allocate(576);
                    socket.receive(byteBuffer);
                    byteBuffer.flip();
                    byte[] messageBytes = new byte[byteBuffer.remaining()];
                    byteBuffer.get(messageBytes);
                    String payload = new String(messageBytes);
                    log.info("Got message: {}", payload);
                } catch (IOException e) {
                    log.info("Error receiving message", e);
                }
            }
        });

        for (int i = 0; i < 100; i++) {
            log.info("Sending {}", i);
            String message = "Ping # " + i;
            byte[] messageBytes = message.getBytes();
            ByteBuffer byteBuffer = ByteBuffer.wrap(messageBytes);
            socket.send(byteBuffer, new InetSocketAddress(targetHost, targetPort));
            Thread.sleep(1000);
        }
        socket.close();
    }

}
