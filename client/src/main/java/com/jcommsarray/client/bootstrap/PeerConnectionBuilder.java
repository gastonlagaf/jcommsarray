package com.jcommsarray.client.bootstrap;

import com.jcommsarray.client.ice.IceConnector;
import com.jcommsarray.client.ice.model.IceRole;
import com.jcommsarray.client.ice.transfer.CandidateTransferOperator;
import com.jcommsarray.client.model.ConnectResult;
import com.jcommsarray.client.protocol.TurnAwareClientProtocol;
import com.jcommsarray.test.protocol.ClientProtocol;
import lombok.RequiredArgsConstructor;

import java.net.InetSocketAddress;
import java.util.function.Consumer;
import java.util.function.Function;

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
