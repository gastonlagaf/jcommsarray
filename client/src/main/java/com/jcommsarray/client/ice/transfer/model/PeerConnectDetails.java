package com.jcommsarray.client.ice.transfer.model;

import com.jcommsarray.client.ice.model.Candidate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.SortedSet;
import java.util.TreeSet;

@Getter
@RequiredArgsConstructor
public class PeerConnectDetails {

    public static final PeerConnectDetails INSTANCE = new PeerConnectDetails("stub", new TreeSet<>());

    private final String password;

    private final SortedSet<Candidate> candidates;

}
