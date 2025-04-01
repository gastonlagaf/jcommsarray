package com.gastonlagaf.udp.client.ice.transfer;

import com.gastonlagaf.udp.client.ice.model.Candidate;

import java.util.List;

public interface CandidateTransferOperator {

    List<Candidate> exchange(String sourceContactId, String targetContactId, List<Candidate> candidates);

}
