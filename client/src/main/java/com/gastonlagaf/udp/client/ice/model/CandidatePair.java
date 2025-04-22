package com.gastonlagaf.udp.client.ice.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
public class CandidatePair implements Comparable<CandidatePair> {

    private final Candidate localCandidate;

    private final Candidate opponentCandidate;

    private final Integer priority;

    @Setter
    private CandidatePairState state;

    private Boolean valid;

    public CandidatePair(IceRole iceRole, Candidate localCandidate, Candidate opponentCandidate) {
        this.localCandidate = localCandidate;
        this.opponentCandidate = opponentCandidate;
        this.priority = getPriority(iceRole);
        this.state = CandidatePairState.FROZEN;
        this.valid = false;
    }

    public void markValid() {
        this.valid = true;
    }

    private Integer getPriority(IceRole iceRole) {
        int controllingPriority = IceRole.CONTROLLING.equals(iceRole) ? localCandidate.getPriority() : opponentCandidate.getPriority();
        int controlledPriority = IceRole.CONTROLLING.equals(iceRole) ? opponentCandidate.getPriority() : localCandidate.getPriority();
        return 2 ^ 32 * Math.min(controllingPriority, controlledPriority)
                + 2 * Math.max(controllingPriority, controlledPriority)
                + (controllingPriority > controlledPriority ? 1 : 0);
    }

    @Override
    public int hashCode() {
        return Objects.hash(localCandidate, opponentCandidate, priority, state, valid);
    }

    @Override
    public int compareTo(CandidatePair o) {
        return o.priority.compareTo(this.priority);
    }
}
