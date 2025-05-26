package com.jcommsarray.client.bootstrap;

import com.jcommsarray.signaling.model.AddressCandidate;
import com.jcommsarray.client.ice.IceConnector;
import com.jcommsarray.client.ice.impl.DefaultIceConnector;
import com.jcommsarray.client.ice.model.Candidate;
import com.jcommsarray.client.ice.model.IceProperties;
import com.jcommsarray.client.ice.model.IceRole;
import com.jcommsarray.client.model.ClientProperties;
import com.jcommsarray.client.model.ConnectResult;
import com.jcommsarray.client.protocol.TurnAwareClientProtocol;
import com.jcommsarray.client.protocol.ProtocolInitializer;
import com.jcommsarray.client.protocol.PureProtocol;
import com.jcommsarray.test.protocol.ClientProtocol;
import lombok.RequiredArgsConstructor;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@RequiredArgsConstructor
public class ClientSession<T extends ClientProtocol<?>> {

    private final ClientBootstrap<T> clientBootstrap;

    private String opponentId;

    private IceRole iceRole;

    private InetSocketAddress targetAddress;

    private IceConnector iceConnector;

    private Function<TurnAwareClientProtocol<?>, T> connectionMapper;

    public ClientSession<T> as(IceRole iceRole) {
        this.iceRole = iceRole;
        return this;
    }

    public ClientSession<T> connectTo(String opponentId) {
        this.opponentId = opponentId;
        return this;
    }

    public ClientSession<T> connectTo(InetSocketAddress targetAddress) {
        this.targetAddress = targetAddress;
        return this;
    }

    public ClientSession<T> mapEstablishedConnection(Function<TurnAwareClientProtocol<?>, T> connectionMapper) {
        this.connectionMapper = connectionMapper;
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
        IceConnector iceConnector = Optional.ofNullable(this.iceConnector).orElseGet(() -> {
            this.iceConnector = initiateIceConnector();
            return this.iceConnector;
        });
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

        TurnAwareClientProtocol<?> turnAwareClientProtocol = getBaseClientProtocol();
        return CompletableFuture.completedFuture(turnAwareClientProtocol)
                .thenApply(it -> {
                    T protocol = connectionMapper.apply(it);
                    return new ConnectResult<>(targetAddress, protocol);
                });
    }

    private TurnAwareClientProtocol<?> getBaseClientProtocol() {
        ProtocolInitializer protocolInitializer = new ProtocolInitializer(40000, 60000);
        ClientProperties clientProperties = new ClientProperties(
                null,
                targetAddress,
                clientBootstrap.stunAddress,
                clientBootstrap.turnAddress,
                clientBootstrap.socketTimeout.toMillis()
        );
        return protocolInitializer.init(
                clientProperties,
                it -> new PureProtocol(clientBootstrap.sockets, null, it, false)
        );
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
        if (null == clientBootstrap.hostId) {
            throw new IllegalArgumentException("Host ID is not specified");
        }
        if (null == clientBootstrap.signalingUri && null == clientBootstrap.candidateTransferOperator) {
            throw new IllegalArgumentException("Unable to send contact information, as signaling server is not specified");
        }
        if (null == iceRole) {
            throw new IllegalArgumentException("Unable to send contact information, as iceRole is not specified");
        }
    }

    private IceConnector initiateIceConnector() {
        IceProperties iceProperties = new IceProperties(
                clientBootstrap.hostId, opponentId, iceRole, 1, 3, 40000, 60000
        );
        ClientProperties clientProperties = new ClientProperties(
                null, null, clientBootstrap.stunAddress, clientBootstrap.turnAddress,
                Optional.ofNullable(clientBootstrap.socketTimeout).orElseGet(() -> Duration.ofSeconds(5L)).toMillis()
        );

        return new DefaultIceConnector(
                clientBootstrap.sockets,
                iceProperties,
                clientProperties,
                clientBootstrap.candidateTransferOperator
        );
    }

}
