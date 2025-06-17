package com.jcommsarray.client.ice.transfer;

import com.jcommsarray.client.ice.model.Candidate;
import com.jcommsarray.client.ice.transfer.model.PeerConnectDetails;

import java.io.Closeable;
import java.util.SortedSet;

public interface CandidateTransferOperator extends Closeable {

    PeerConnectDetails exchange(String sourceContactId, String targetContactId, SortedSet<Candidate> candidates, String password);

}
