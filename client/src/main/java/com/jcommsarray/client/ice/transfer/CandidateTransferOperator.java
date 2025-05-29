package com.jcommsarray.client.ice.transfer;

import com.jcommsarray.client.ice.model.Candidate;

import java.io.Closeable;
import java.util.SortedSet;

public interface CandidateTransferOperator extends Closeable {

    SortedSet<Candidate> exchange(String sourceContactId, String targetContactId, SortedSet<Candidate> candidates);

}
