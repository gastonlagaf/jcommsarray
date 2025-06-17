package com.jcommsarray.client.ice.check;

import com.jcommsarray.client.ice.model.*;
import com.jcommsarray.client.ice.protocol.IceProtocol;
import com.jcommsarray.client.model.ConnectResult;
import com.jcommsarray.turn.exception.StunProtocolException;
import com.jcommsarray.turn.model.*;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class Checklist {

    private static final Long TA = 50L;

    private static final Long DEFAULT_RTO = 500L;

    private static final Integer EXCEEDED_RETRIES_COUNT = 0;

    private final IceSession iceSession;

    private final SortedSet<CandidatePair> pairs;

    private final Long rto;

    private final Integer retries;

    private final CompletableFuture<ConnectResult<IceProtocol>> future;

    private final AtomicReference<ChecklistState> state = new AtomicReference<>(ChecklistState.RUNNING);

    private final SortedSet<CandidatePair> validList = new TreeSet<>();

    private final Map<Integer, AtomicInteger> retryCounters = new HashMap<>();

    private final ReentrantLock lock = new ReentrantLock();

    private final String subscriberId;

    public Checklist(Integer retries, IceSession iceSession, SortedSet<CandidatePair> pairs, CompletableFuture<ConnectResult<IceProtocol>> future, String subscriberId) {
        this.iceSession = iceSession;
        if (pairs.isEmpty()) {
            throw new IllegalArgumentException("Pairs must not be empty");
        }
        this.pairs = pairs;
        this.rto = Math.max(DEFAULT_RTO, TA * pairs.size());
        this.retries = retries;
        this.future = future;
        this.subscriberId = subscriberId;
    }

    public CompletableFuture<ConnectResult<IceProtocol>> check() {
        Iterator<CandidatePair> iterator = pairs.iterator();
        proceed(iterator);
        return future;
    }

    private void proceed(Iterator<CandidatePair> iterator) {
        CandidatePair candidatePair = iterator.next();
        checkPair(candidatePair);
        if (iterator.hasNext()) {
            schedule(() -> this.proceed(iterator), TA);
        }
    }

    private void checkPair(CandidatePair candidatePair) {
        if (CandidatePairState.FROZEN.equals(candidatePair.getState())) {
            log.info("Unfroze pair {} -> {}", candidatePair.getLocalCandidate().getActualAddress(), candidatePair.getOpponentCandidate().getActualAddress());
            candidatePair.setState(CandidatePairState.WAITING);
        }
        sendAndReceive(candidatePair, false).handle((it, ex) -> handleCheckResponse(it, ex, candidatePair));

        candidatePair.setState(CandidatePairState.IN_PROGRESS);
    }

    private synchronized Message handleCheckResponse(Message response, Throwable ex, CandidatePair candidatePair) {
        if (null != ex) {
            log.warn("Got error while checking pair {} - {}: {}", candidatePair.getLocalCandidate().getActualAddress(), candidatePair.getOpponentCandidate().getActualAddress(), ex.getMessage());
            int retriesRemaining = retryCounters.computeIfAbsent(
                    candidatePair.getPriority(), key -> new AtomicInteger(retries)
            ).decrementAndGet();
            if (EXCEEDED_RETRIES_COUNT == retriesRemaining) {
                candidatePair.setState(CandidatePairState.FAILED);
            } else if (ChecklistState.RUNNING.equals(state.get())) {
                schedule(() -> checkPair(candidatePair), rto);
            }
        } else {
            candidatePair.setState(CandidatePairState.SUCCEEDED);
            validList.add(candidatePair);
            log.info("Pair {} - {} sent to nomination list", candidatePair.getLocalCandidate().getActualAddress(), candidatePair.getOpponentCandidate().getActualAddress());
        }
        if (!CandidatePairState.IN_PROGRESS.equals(candidatePair.getState())) {
            launchNominationIfRequired();
        }
        return response;
    }

    private void launchNominationIfRequired() {
        if (IceRole.CONTROLLED.equals(iceSession.getRole())) {
            return;
        }
        try {
            lock.lock();
            boolean shouldNominate = pairs.stream().allMatch(
                    it -> CandidatePairState.SUCCEEDED.equals(it.getState()) || CandidatePairState.FAILED.equals(it.getState())
            ) && !future.isDone();
            if (shouldNominate) {
                Iterator<CandidatePair> iterator = validList.iterator();
                nominate(iterator);
            }
        } finally {
            lock.unlock();
        }
    }

    private void nominate(Iterator<CandidatePair> iterator) {
        log.info("Initializing nomination procedure");
        if (validList.isEmpty() || !iterator.hasNext()) {
            state.set(ChecklistState.FAILED);
            future.completeExceptionally(new IllegalStateException("No valid candidates found"));
        }
        CandidatePair candidatePair = iterator.next();
        sendAndReceive(candidatePair, true)
                .exceptionally(ex -> {
                    nominate(iterator);
                    return null;
                })
                .thenAccept(it -> {
                    if (future.isDone()) {
                        return;
                    }
                    state.set(ChecklistState.COMPLETED);
                    ConnectResult<IceProtocol> result = new ConnectResult<>(
                            candidatePair.getOpponentCandidate().getActualAddress(),
                            candidatePair.getLocalCandidate().getIceProtocol()
                    );
                    log.info("Found result candidate: {} - {}", candidatePair.getLocalCandidate().getActualAddress(), candidatePair.getOpponentCandidate().getActualAddress());
                    future.complete(result);
                });

    }

    private CompletableFuture<Message> sendAndReceive(CandidatePair candidatePair, Boolean nominate) {
        Candidate localCandidate = candidatePair.getLocalCandidate();

        Message message = prepareBindingRequest(candidatePair, nominate);
        return localCandidate.getIceProtocol().getClient()
                .sendAndReceive(localCandidate.getHostAddress(), candidatePair.getOpponentCandidate().getActualAddress(), message)
                .thenApply(it -> {
                    ErrorCodeAttribute errorAttribute = it.getAttributes().get(KnownAttributeName.ERROR_CODE);
                    if (null == errorAttribute) {
                        return it;
                    }

                    StunProtocolException ex = new StunProtocolException(errorAttribute.getReasonPhrase(), errorAttribute.getCode());
                    if (ErrorCode.ROLE_CONFLICT.getCode().equals(errorAttribute.getCode())) {
                        state.set(ChecklistState.FAILED);
                        future.completeExceptionally(ex);
                    }
                    throw ex;
                });
    }

    private Message prepareBindingRequest(CandidatePair candidatePair, Boolean nominate) {
        MessageHeader messageHeader = new MessageHeader(MessageType.BINDING_REQUEST);
        Map<Integer, MessageAttribute> attributes = new HashMap<>();
        attributes.put(KnownAttributeName.PRIORITY.getCode(), new IntegerAttribute(KnownAttributeName.PRIORITY.getCode(), candidatePair.getPriority()));

        KnownAttributeName roleAttributeName = IceRole.CONTROLLING.equals(iceSession.getRole())
                ? KnownAttributeName.ICE_CONTROLLING
                : KnownAttributeName.ICE_CONTROLLED;
        LongAttribute roleAttribute = new LongAttribute(roleAttributeName.getCode(), iceSession.getTieBreaker());
        attributes.put(roleAttribute.getType(), roleAttribute);

        if (nominate) {
            attributes.put(KnownAttributeName.USE_CANDIDATE.getCode(), new FlagAttribute(KnownAttributeName.USE_CANDIDATE.getCode()));
        }
        return new Message(messageHeader, attributes);
    }

    private void schedule(Runnable runnable, Long delayMillis) {
        Executor delayedExecutor = CompletableFuture.delayedExecutor(delayMillis, TimeUnit.MILLISECONDS);
        CompletableFuture.runAsync(runnable, delayedExecutor);
    }

}
