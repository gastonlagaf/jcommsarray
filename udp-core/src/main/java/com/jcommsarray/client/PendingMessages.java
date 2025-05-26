package com.jcommsarray.client;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalListener;
import com.github.benmanes.caffeine.cache.Scheduler;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;

public class PendingMessages<T> {

    private final Long timeoutMillis;

//    private final Cache<String, CompletableFuture<T>> messagesAwaitMap;

    private final Map<String, CompletableFuture<T>> messagesAwaitMap;

    public PendingMessages(Long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
        this.messagesAwaitMap = new ConcurrentHashMap<>();
//        this.messagesAwaitMap = Caffeine.newBuilder()
//                .scheduler(Scheduler.systemScheduler())
//                .expireAfterWrite(Duration.ofMillis(timeoutMillis))
//                .removalListener(
//                        (RemovalListener<String, CompletableFuture<T>>) (key, value, cause) -> {
//                            if (null != value && cause.wasEvicted()) {
//                                value.completeExceptionally(new TimeoutException());
//                            }
//                        })
//                .build();
    }

    public CompletableFuture<T> put(String txId) {
        CompletableFuture<T> result = new CompletableFuture<T>()
                .orTimeout(timeoutMillis, TimeUnit.MILLISECONDS)
                .whenComplete((it, ex) -> messagesAwaitMap.remove(txId));
        messagesAwaitMap.put(txId, result);
        return result;
    }

    public Boolean fail(String txId, String message) {
        return Optional.ofNullable(messagesAwaitMap.remove(txId))
                .map(it -> it.completeExceptionally(new RuntimeException(message)))
                .orElse(false);
    }

    public Boolean complete(String txId, T message) {
        return Optional.ofNullable(messagesAwaitMap.remove(txId))
                .map(it -> it.complete(message))
                .orElse(false);
    }

}
