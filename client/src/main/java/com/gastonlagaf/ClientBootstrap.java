package com.gastonlagaf;

import com.gastonlagaf.pure.PureProtocol;
import com.gastonlagaf.stun.client.StunClient;
import com.gastonlagaf.stun.client.impl.UdpStunClient;
import com.gastonlagaf.stun.client.model.StunClientProperties;
import com.gastonlagaf.stun.model.DefaultMessageAttribute;
import com.gastonlagaf.stun.model.KnownAttributeName;
import com.gastonlagaf.turn.client.TurnClient;
import com.gastonlagaf.udp.client.BaseUdpClient;
import com.gastonlagaf.udp.client.UdpClient;
import com.gastonlagaf.udp.socket.UdpSockets;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.time.Duration;
import java.util.List;

@Slf4j
public class ClientBootstrap {

    @SneakyThrows
    public static void main(String[] args) {
        if (args.length == 0) {
            throw new IllegalArgumentException("Please provide mode");
        }
        String mode = args[0];
        String interfaceHost = NetworkInterface.getByName("lo0").inetAddresses()
                .filter(it -> it instanceof Inet4Address)
                .findFirst()
                .orElseThrow()
                .getHostName();

        switch (mode) {
            case "turn" -> {
                Integer port = 45198;
                turnCommunicate(interfaceHost, port, "localhost", 3478, "localhost", 50321);
            }
            case "turn-channel" -> {
                Integer port = 45199;
                turnChannelCommunicate(interfaceHost, port, "localhost", 3478, "localhost", 50321);
            }
            case "pure" -> {
                Integer port = 50321;
                rawCommunication(interfaceHost, port);
            }
        }
    }

    private static void rawCommunication(String interfaceIp, Integer port) {
        PureProtocol pureProtocol = new PureProtocol(1);
        pureProtocol.start(new InetSocketAddress(interfaceIp, port));
    }

    @SneakyThrows
    private static void turnCommunicate(String interfaceIp, Integer port, String serverHost, Integer serverPort, String targetHost, Integer targetPort) {
        StunClientProperties properties = new StunClientProperties(interfaceIp, port, serverHost, serverPort, 5000);
        StunTurnClientProtocol messageHandler = new StunTurnClientProtocol(properties.getSocketTimeout().longValue(), ((receiverAddress, senderAddress, message) -> {
            DefaultMessageAttribute dataAttribute = message.getAttributes().get(KnownAttributeName.DATA);
            byte[] data = dataAttribute.getValue();
            String messageText = new String(data);
            log.info("Got message {}", messageText);
        }));

        StunClient stunClient = new UdpStunClient(properties, messageHandler);
        TurnClient turnClient = stunClient.initializeTurnSession();

        InetSocketAddress targetAddress = new InetSocketAddress(targetHost, targetPort);
        turnClient.createPermission(List.of(targetAddress));

        for (int i = 0; i < 100; i++) {
            String message = "Ping " + i;
            turnClient.send(targetAddress, message.getBytes());
            Thread.sleep(Duration.ofSeconds(1));
        }

        turnClient.close();
        stunClient.close();
    }

    @SneakyThrows
    private static void turnChannelCommunicate(String interfaceIp, Integer port, String serverHost, Integer serverPort, String targetHost, Integer targetPort) {
        StunClientProperties properties = new StunClientProperties(interfaceIp, port, serverHost, serverPort, 5000);
        StunTurnClientProtocol messageHandler = new StunTurnClientProtocol(properties.getSocketTimeout().longValue(), ((receiverAddress, senderAddress, message) -> {
            DefaultMessageAttribute dataAttribute = message.getAttributes().get(KnownAttributeName.DATA);
            byte[] data = dataAttribute.getValue();
            String messageText = new String(data);
            log.info("Got message {}", messageText);
        }));

        StunClient stunClient = new UdpStunClient(properties, messageHandler);
        TurnClient turnClient = stunClient.initializeTurnSession();

        InetSocketAddress targetAddress = new InetSocketAddress(targetHost, targetPort);
        Integer channel = turnClient.createChannel(targetAddress);

        for (int i = 0; i < 100; i++) {
            String message = "Ping " + i;
            turnClient.send(channel, message.getBytes());
            Thread.sleep(Duration.ofSeconds(1));
        }

        turnClient.close();
        stunClient.close();
    }

}
