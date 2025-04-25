package com.gastonlagaf.udp.client.ice;

import com.gastonlagaf.udp.client.ice.model.Candidate;
import com.gastonlagaf.udp.client.ice.protocol.IceProtocol;
import com.gastonlagaf.udp.client.model.ConnectResult;

import java.util.SortedSet;
import java.util.concurrent.CompletableFuture;

public interface IceConnector {

    CompletableFuture<ConnectResult<IceProtocol>> connect(String opponentId);

    CompletableFuture<ConnectResult<IceProtocol>> connect(String opponentId, SortedSet<Candidate> opponentCandidates);

    SortedSet<Candidate> getLocalCandidates();

}
