package com.gastonlagaf.udp.client.bootstrap;

import com.gastonlagaf.signaling.model.AddressCandidate;
import com.gastonlagaf.signaling.model.SignalingSubscriber;
import com.gastonlagaf.udp.client.ice.IceConnector;
import com.gastonlagaf.udp.client.ice.impl.DefaultIceConnector;
import com.gastonlagaf.udp.client.ice.model.Candidate;
import com.gastonlagaf.udp.client.ice.model.IceProperties;
import com.gastonlagaf.udp.client.ice.model.IceRole;
import com.gastonlagaf.udp.client.ice.transfer.CandidateTransferOperator;
import com.gastonlagaf.udp.client.ice.transfer.impl.DefaultCandidateTransferOperator;
import com.gastonlagaf.udp.client.model.ClientProperties;
import com.gastonlagaf.udp.client.model.ConnectResult;
import com.gastonlagaf.udp.client.model.SignalingProperties;
import com.gastonlagaf.udp.client.protocol.BaseClientProtocol;
import com.gastonlagaf.udp.client.protocol.ProtocolInitializer;
import com.gastonlagaf.udp.client.protocol.PureProtocol;
import com.gastonlagaf.udp.client.signaling.SignalingClient;
import com.gastonlagaf.udp.client.signaling.impl.DefaultSignalingClient;
import com.gastonlagaf.udp.protocol.ClientProtocol;
import com.gastonlagaf.udp.socket.UdpSockets;
import lombok.RequiredArgsConstructor;

import java.net.InetSocketAddress;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@RequiredArgsConstructor
public class ClientBootstrap<T extends ClientProtocol<?>> {

    private final UdpSockets sockets;

    private String hostId;

    private String opponentId;

    private IceRole iceRole;

    private InetSocketAddress targetAddress;

    private InetSocketAddress stunAddress;

    private InetSocketAddress turnAddress;

    private Duration socketTimeout;

    private URI signalingUri;

    private Duration signalingTimeout;

    private CandidateTransferOperator candidateTransferOperator;

    private IceConnector iceConnector;

    private Function<BaseClientProtocol<?>, T> connectionMapper;

    public ClientBootstrap<T> withHostId(String hostId) {
        this.hostId = hostId;
        return this;
    }

    public ClientBootstrap<T> as(IceRole iceRole) {
        this.iceRole = iceRole;
        return this;
    }

    public ClientBootstrap<T> connectTo(String opponentId) {
        this.opponentId = opponentId;
        return this;
    }

    public ClientBootstrap<T> connectTo(InetSocketAddress targetAddress) {
        this.targetAddress = targetAddress;
        return this;
    }

    public ClientBootstrap<T> useStun(InetSocketAddress stunAddress) {
        this.stunAddress = stunAddress;
        return this;
    }

    public ClientBootstrap<T> useTurn(InetSocketAddress turnAddress) {
        this.turnAddress = turnAddress;
        return this;
    }

    public ClientBootstrap<T> useSocketTimeout(Duration socketTimeout) {
        this.socketTimeout = socketTimeout;
        return this;
    }

    public ClientBootstrap<T> useSignaling(URI signalingUri) {
        return useSignaling(signalingUri, null);
    }

    public ClientBootstrap<T> useSignaling(URI signalingUri, Duration signalingTimeout) {
        this.signalingUri = signalingUri;
        Optional.ofNullable(signalingTimeout).ifPresent(it -> this.signalingTimeout = signalingTimeout);
        return this;
    }

    public ClientBootstrap<T> mapEstablishedConnection(Function<BaseClientProtocol<?>, T> connectionMapper) {
        this.connectionMapper = connectionMapper;
        return this;
    }

    public ClientBootstrap<T> useCandidateTransferOperator(CandidateTransferOperator candidateTransferOperator) {
        this.candidateTransferOperator = candidateTransferOperator;
        return this;
    }

    public List<AddressCandidate> getAddressCandidates() {
        if (null == iceConnector) {
            validateIceBootstrap();

            this.iceConnector = initiateIceConnector();
        }
        return this.iceConnector.getLocalCandidates().stream()
                .map(it -> new AddressCandidate(it.getPriority(), it.getType().name(), it.getActualAddress()))
                .toList();
    }

    public CompletableFuture<ConnectResult<T>> connect(SortedSet<Candidate> candidates) {
        validate();
        if (null == opponentId) {
            throw new IllegalArgumentException("Bootstrap is not configured for ice connection");
        }
        IceConnector iceConnector = Optional.ofNullable(this.iceConnector).orElseGet(this::initiateIceConnector);
        return iceConnector.connect(opponentId, candidates)
                .thenApplyAsync(it -> {
                    T protocol = connectionMapper.apply(it.getProtocol());
                    return new ConnectResult<>(it.getOpponentAddress(), protocol);
                });
    }

    public CompletableFuture<ConnectResult<T>> connect() {
        validate();
        if (null != opponentId) {
            IceConnector iceConnector = Optional.ofNullable(this.iceConnector).orElseGet(this::initiateIceConnector);
            return iceConnector.connect(opponentId)
                    .thenApplyAsync(it -> {
                        T protocol = connectionMapper.apply(it.getProtocol());
                        return new ConnectResult<>(it.getOpponentAddress(), protocol);
                    });
        }

        ProtocolInitializer protocolInitializer = new ProtocolInitializer(40000, 60000);
        ClientProperties clientProperties = new ClientProperties(
                null, targetAddress, stunAddress, turnAddress, socketTimeout.toMillis()
        );
        BaseClientProtocol<?> baseClientProtocol = protocolInitializer.init(
                clientProperties, it -> new PureProtocol(sockets, null, it, false)
        );
        return CompletableFuture.completedFuture(baseClientProtocol)
                .thenApply(it -> {
                    T protocol = connectionMapper.apply(it);
                    return new ConnectResult<>(targetAddress, protocol);
                });
    }

    private void validate() {
        if (null == targetAddress && null == opponentId) {
            throw new IllegalArgumentException("Connection target is not specified. Set either opponentId or targetAddress");
        }
        if (null == connectionMapper) {
            throw new IllegalArgumentException("Can't map connection to target protocol");
        }
        if (null == iceConnector) {
            validateIceBootstrap();
        }
    }

    private void validateIceBootstrap() {
        if (null == opponentId) {
            return;
        }
        if (null == hostId) {
            throw new IllegalArgumentException("Host ID is not specified");
        }
        if (null == signalingUri && null == candidateTransferOperator) {
            throw new IllegalArgumentException("Unable to send contact information, as signaling server is not specified");
        }
        if (null == iceRole) {
            throw new IllegalArgumentException("Unable to send contact information, as iceRole is not specified");
        }
    }

    private IceConnector initiateIceConnector() {
        CandidateTransferOperator transferOperator = Optional.of(iceRole)
                .filter(IceRole.CONTROLLING::equals)
                .flatMap(it -> Optional.ofNullable(candidateTransferOperator))
                .orElseGet(this::initiateDefaultCandidateTransferOperator);

        IceProperties iceProperties = new IceProperties(
                hostId, opponentId, iceRole, 1, 3, 40000, 60000
        );
        ClientProperties clientProperties = new ClientProperties(
                null, null, stunAddress, turnAddress,
                Optional.ofNullable(socketTimeout).orElseGet(() -> Duration.ofSeconds(5L)).toMillis()
        );

        return new DefaultIceConnector(sockets, iceProperties, clientProperties, transferOperator);
    }

    private CandidateTransferOperator initiateDefaultCandidateTransferOperator() {
        SignalingProperties signalingProperties = new SignalingProperties(
                signalingUri, Optional.ofNullable(signalingTimeout).orElseGet(() -> Duration.ofSeconds(10L))
        );
        SignalingClient signalingClient = new DefaultSignalingClient(
                signalingProperties, new SignalingSubscriber(hostId, List.of()), null
        );

        return new DefaultCandidateTransferOperator(signalingClient);
    }

}
