package com.gastonlagaf.udp.client.bootstrap;

import com.gastonlagaf.udp.client.ice.IceConnector;
import com.gastonlagaf.udp.client.ice.model.IceRole;
import com.gastonlagaf.udp.client.ice.transfer.CandidateTransferOperator;
import com.gastonlagaf.udp.client.model.ConnectResult;
import com.gastonlagaf.udp.client.protocol.TurnAwareClientProtocol;
import com.gastonlagaf.udp.protocol.ClientProtocol;
import lombok.RequiredArgsConstructor;

import java.net.InetSocketAddress;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class PeerConnectionBuilder<T extends ClientProtocol<?>> {

    private final ExchangeSession<T> exchangeSession;

    private String opponentId;

    private IceRole iceRole;

    private InetSocketAddress targetAddress;

    private CandidateTransferOperator candidateTransferOperator;

    private IceConnector iceConnector;

    private Function<TurnAwareClientProtocol<?>, T> connectionMapper;

    private Consumer<PeerConnection<T>> onEstablishedConnection;

    private ConnectResult<T> connectResult;

    public PeerConnectionBuilder<T> as(IceRole iceRole) {
        this.iceRole = iceRole;
        return this;
    }

    public PeerConnectionBuilder<T> onEstablishedConnection(Consumer<PeerConnection<T>> onEstablishedConnection) {
        this.onEstablishedConnection = onEstablishedConnection;
        return this;
    }

    public PeerConnectionBuilder<T> connectTo(String opponentId) {
        this.opponentId = opponentId;
        return this;
    }

    public PeerConnectionBuilder<T> connectTo(InetSocketAddress targetAddress) {
        this.targetAddress = targetAddress;
        return this;
    }

    public PeerConnectionBuilder<T> mapEstablishedConnection(Function<TurnAwareClientProtocol<?>, T> connectionMapper) {
        this.connectionMapper = connectionMapper;
        return this;
    }

    public PeerConnectionBuilder<T> useCandidateTransferOperator(CandidateTransferOperator candidateTransferOperator) {
        this.candidateTransferOperator = candidateTransferOperator;
        return this;
    }

    public PeerConnection<T> build() {
        return new PeerConnection<>(
                exchangeSession, opponentId, iceRole, candidateTransferOperator,
                connectionMapper, onEstablishedConnection, targetAddress
        );
    }

}
