package com.gastonlagaf.udp.client.protocol;

import com.gastonlagaf.udp.client.BaseUdpClient;
import com.gastonlagaf.udp.client.PendingMessages;
import com.gastonlagaf.udp.client.UdpClient;
import com.gastonlagaf.udp.client.turn.TurnClientProtocol;
import com.gastonlagaf.udp.discovery.InternetDiscovery;
import com.gastonlagaf.udp.client.model.ClientProperties;
import com.gastonlagaf.udp.client.stun.StunClientProtocol;
import com.gastonlagaf.udp.client.stun.client.StunClient;
import com.gastonlagaf.udp.client.turn.proxy.TurnProxy;
import com.gastonlagaf.udp.protocol.ClientProtocol;
import com.gastonlagaf.udp.protocol.model.UdpPacketHandlerResult;
import com.gastonlagaf.udp.socket.UdpSockets;
import com.gastonlagaf.udp.turn.model.NatBehaviour;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public abstract class BaseClientProtocol<T> implements ClientProtocol<T> {

    private static final Set<NatBehaviour> TURN_REQUIRED_NAT_BEHAVIOURS = Set.of(
            NatBehaviour.ADDRESS_DEPENDENT, NatBehaviour.ADDRESS_AND_PORT_DEPENDENT
    );

    protected final ClientProperties clientProperties;

    protected final PendingMessages<T> pendingMessages;

    protected final NatBehaviour natBehaviour;

    protected final UdpSockets sockets;

    protected final UdpClient<T> client;

    protected SelectionKey selectionKey;

    private final TurnClientProtocol<T> turnClientProtocol;

    public BaseClientProtocol(UdpSockets sockets) {
        this(null, null, sockets);
    }

    public BaseClientProtocol(BaseClientProtocol<?> baseProtocol) {
        this.clientProperties = baseProtocol.clientProperties;
        this.natBehaviour = baseProtocol.natBehaviour;
        this.sockets = baseProtocol.sockets;

        UdpClient<T> udpClient;
        if (null != baseProtocol.turnClientProtocol) {
            this.turnClientProtocol = new TurnClientProtocol<>(baseProtocol.turnClientProtocol, this);
            udpClient = new TurnProxy<>(this, this.turnClientProtocol);
            this.selectionKey = baseProtocol.turnClientProtocol.selectionKey;
        } else {
            this.turnClientProtocol = null;
            udpClient = baseProtocol instanceof TurnClientProtocol<?>
                ? (UdpClient<T>) baseProtocol.client
                : new BaseUdpClient<>(sockets, this, this.clientProperties.getHostAddress());
            this.selectionKey = baseProtocol.selectionKey;
        }
        this.client = baseProtocol instanceof TurnClientProtocol<?> ? udpClient : createUdpClient(udpClient);
        this.pendingMessages = new PendingMessages<>(clientProperties.getSocketTimeout());

        sockets.getRegistry().switchProtocol(
                selectionKey,
                null == this.turnClientProtocol ? this : this.turnClientProtocol
        );
    }

    public BaseClientProtocol(NatBehaviour natBehaviour, ClientProperties clientProperties, UdpSockets sockets) {
        this.clientProperties = Optional.ofNullable(clientProperties).orElseGet(this::getClientProperties);
        this.natBehaviour = Optional.ofNullable(natBehaviour).orElseGet(() -> getNatBehaviour(this.clientProperties));
        this.sockets = sockets;

        UdpClient<T> udpClient;
        if (!TURN_REQUIRED_NAT_BEHAVIOURS.contains(this.natBehaviour)) {
            this.turnClientProtocol = null;
            udpClient = new BaseUdpClient<>(sockets, this, this.clientProperties.getHostAddress());
        } else {
            this.turnClientProtocol = new TurnClientProtocol<>(sockets, this, clientProperties);
            udpClient = new TurnProxy<>(this,  turnClientProtocol);
        }
        this.client = createUdpClient(udpClient);

        this.pendingMessages = new PendingMessages<>(this.clientProperties.getSocketTimeout());
    }

    protected abstract String getCorrelationId(T message);

    protected abstract UdpClient<T> createUdpClient(UdpClient<T> udpClient);

    public abstract UdpPacketHandlerResult handle(InetSocketAddress receiverAddress, InetSocketAddress senderAddress, T message);

    public UdpPacketHandlerResult handle(InetSocketAddress receiverAddress, InetSocketAddress senderAddress, ByteBuffer buffer) {
        T message = deserialize(receiverAddress, senderAddress, buffer);
        return handle(receiverAddress, senderAddress, message);
    }

    @Override
    public CompletableFuture<T> awaitResult(T message) {
        String correlationId = getCorrelationId(message);
        return pendingMessages.put(correlationId);
    }

    @Override
    public UdpClient<T> getClient() {
        return client;
    }

    private ClientProperties getClientProperties() {
        InputStream propertiesStream = BaseClientProtocol.class.getClassLoader().getResourceAsStream("udp-client.properties");
        Properties properties = new Properties();
        try {
            properties.load(propertiesStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        InetAddress hostIp = InternetDiscovery.getAddress();
        return new ClientProperties(hostIp, properties);
    }

    private NatBehaviour getNatBehaviour(ClientProperties clientProperties) {
        try (StunClientProtocol stunClientProtocol = new StunClientProtocol(this.sockets, clientProperties)) {
            stunClientProtocol.start();
            StunClient stunClient = (StunClient) stunClientProtocol.getClient();
            return stunClient.checkMappingBehaviour();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void start() {
        if (null != selectionKey) {
            throw new IllegalStateException("Protocol is already bound to socket");
        }
        if (null == turnClientProtocol) {
            this.selectionKey = sockets.getRegistry().register(clientProperties.getHostAddress(), this);
        } else {
            turnClientProtocol.start();
            this.selectionKey = turnClientProtocol.selectionKey;
        }
    }

    @Override
    public void close() throws IOException {
        client.close();
        Optional.ofNullable(selectionKey).ifPresent(it -> sockets.getRegistry().deregister(it));
    }

}
