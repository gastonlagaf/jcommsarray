package com.jcommsarray.client.ice;

import com.jcommsarray.client.ice.model.Candidate;
import com.jcommsarray.client.ice.protocol.IceProtocol;
import com.jcommsarray.client.ice.transfer.model.PeerConnectDetails;
import com.jcommsarray.client.model.ConnectResult;

import java.util.SortedSet;
import java.util.concurrent.CompletableFuture;

public interface IceConnector {

    CompletableFuture<ConnectResult<IceProtocol>> connect(String opponentId);

    CompletableFuture<ConnectResult<IceProtocol>> connect(String opponentId, PeerConnectDetails peerConnectDetails);

    SortedSet<Candidate> getLocalCandidates();

}
