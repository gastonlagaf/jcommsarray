package com.gastonlagaf.udp.client.ice;

import com.gastonlagaf.udp.client.ice.model.Candidate;
import com.gastonlagaf.udp.client.ice.model.IceConnectResult;
import com.gastonlagaf.udp.client.ice.protocol.IceProtocol;

import java.util.List;
import java.util.SortedSet;
import java.util.concurrent.CompletableFuture;

public interface IceConnector {

    CompletableFuture<IceConnectResult> connect(String opponentId);

    CompletableFuture<IceConnectResult> connect(String opponentId, SortedSet<Candidate> opponentCandidates);

    SortedSet<Candidate> getLocalCandidates();

}
