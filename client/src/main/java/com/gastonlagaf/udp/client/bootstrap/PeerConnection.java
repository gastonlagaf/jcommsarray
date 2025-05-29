package com.gastonlagaf.udp.client.bootstrap;

import com.gastonlagaf.signaling.model.AddressCandidate;
import com.gastonlagaf.udp.client.ice.IceConnector;
import com.gastonlagaf.udp.client.ice.impl.DefaultIceConnector;
import com.gastonlagaf.udp.client.ice.model.Candidate;
import com.gastonlagaf.udp.client.ice.model.IceProperties;
import com.gastonlagaf.udp.client.ice.model.IceRole;
import com.gastonlagaf.udp.client.ice.transfer.CandidateTransferOperator;
import com.gastonlagaf.udp.client.model.ClientProperties;
import com.gastonlagaf.udp.client.model.ConnectResult;
import com.gastonlagaf.udp.client.protocol.TurnAwareClientProtocol;
import com.gastonlagaf.udp.client.protocol.PureProtocol;
import com.gastonlagaf.udp.protocol.ClientProtocol;
import com.gastonlagaf.udp.turn.model.NatBehaviour;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public class PeerConnection<T extends ClientProtocol<?>> implements Closeable {

    private final ExchangeSession<T> exchangeSession;

    private final String opponentId;

    private final IceRole iceRole;

    private final CandidateTransferOperator candidateTransferOperator;

    private final Function<TurnAwareClientProtocol<?>, T> connectionMapper;

    private final Consumer<PeerConnection<T>> onEstablishedConnection;

    private InetSocketAddress targetAddress;

    private IceConnector iceConnector;

    private ConnectResult<T> connectResult;

    PeerConnection(ExchangeSession<T> exchangeSession, String opponentId, IceRole iceRole, CandidateTransferOperator candidateTransferOperator, Function<TurnAwareClientProtocol<?>, T> connectionMapper, Consumer<PeerConnection<T>> onEstablishedConnection, InetSocketAddress targetAddress) {
        this.exchangeSession = exchangeSession;
        this.opponentId = opponentId;
        this.iceRole = iceRole;
        this.candidateTransferOperator = candidateTransferOperator;
        this.connectionMapper = connectionMapper;
        this.onEstablishedConnection = onEstablishedConnection;
        this.targetAddress = targetAddress;
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
                .thenApplyAsync(this::mapConnectResult);
    }

    public CompletableFuture<ConnectResult<T>> connect() {
        validate();
        if (null != opponentId) {
            IceConnector iceConnector = Optional.ofNullable(this.iceConnector).orElseGet(this::initiateIceConnector);
            return iceConnector.connect(opponentId)
                    .thenApplyAsync(this::mapConnectResult);
        }

        TurnAwareClientProtocol<?> turnAwareClientProtocol = getBaseClientProtocol();
        return CompletableFuture.completedFuture(turnAwareClientProtocol)
                .thenApply(it -> this.mapConnectResult(new ConnectResult<>(targetAddress, it)));
    }

    public T getProtocol() {
        assertConnectionEstablished();
        return connectResult.getProtocol();
    }

    public InetSocketAddress getTargetAddress() {
        assertConnectionEstablished();
        return connectResult.getOpponentAddress();
    }

    @Override
    public void close() throws IOException {
        connectResult.getProtocol().close();
    }

    private void assertConnectionEstablished() {
        if (null == connectResult) {
            throw new IllegalStateException("Connection has not been established yet");
        }
    }

    private TurnAwareClientProtocol<?> getBaseClientProtocol() {
        ProtocolInitializer protocolInitializer = new ProtocolInitializer(exchangeSession.minPort, exchangeSession.maxPort);
        ClientProperties clientProperties = new ClientProperties(
                null,
                targetAddress,
                exchangeSession.stunAddress,
                exchangeSession.turnAddress,
                exchangeSession.socketTimeout.toMillis()
        );
        return protocolInitializer.init(
                clientProperties,
                it -> new PureProtocol(exchangeSession.sockets, NatBehaviour.NO_NAT, it, false)
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
        if (null == exchangeSession.hostId) {
            throw new IllegalArgumentException("Host ID is not specified");
        }
        if (null == iceRole) {
            throw new IllegalArgumentException("Unable to send contact information, as iceRole is not specified");
        }
    }

    private IceConnector initiateIceConnector() {
        IceProperties iceProperties = new IceProperties(
                exchangeSession.hostId, opponentId, iceRole, 1, 3, exchangeSession.minPort, exchangeSession.maxPort
        );
        ClientProperties clientProperties = new ClientProperties(
                null, null, exchangeSession.stunAddress, exchangeSession.turnAddress,
                Optional.ofNullable(exchangeSession.socketTimeout).orElseGet(() -> Duration.ofSeconds(5L)).toMillis()
        );

        return new DefaultIceConnector(
                exchangeSession.sockets,
                iceProperties,
                clientProperties,
                candidateTransferOperator
        );
    }

    private ConnectResult<T> mapConnectResult(ConnectResult<? extends TurnAwareClientProtocol<?>> rawResult) {
        T protocol = connectionMapper.apply(rawResult.getProtocol());
        ConnectResult<T> result = new ConnectResult<>(rawResult.getOpponentAddress(), protocol);
        this.targetAddress = rawResult.getOpponentAddress();
        this.connectResult = result;

        Optional.ofNullable(onEstablishedConnection).ifPresent(it -> it.accept(this));

        return result;
    }

}
