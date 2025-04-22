package com.gastonlagaf.udp.client.ice.transfer;

import com.gastonlagaf.udp.client.ice.model.Candidate;

import java.io.Closeable;
import java.util.List;
import java.util.SortedSet;

public interface CandidateTransferOperator extends Closeable {

    SortedSet<Candidate> exchange(String sourceContactId, String targetContactId, SortedSet<Candidate> candidates);

}
