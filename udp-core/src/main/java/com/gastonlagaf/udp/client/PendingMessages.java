package com.gastonlagaf.udp.client;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalListener;
import com.github.benmanes.caffeine.cache.Scheduler;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

public class PendingMessages<T> {

    private final Cache<String, CompletableFuture<T>> messagesAwaitMap;

    public PendingMessages(Long timeoutMillis) {
        this.messagesAwaitMap = Caffeine.newBuilder()
                .scheduler(Scheduler.systemScheduler())
                .expireAfterWrite(Duration.ofMillis(timeoutMillis))
                .removalListener(
                        (RemovalListener<String, CompletableFuture<T>>) (key, value, cause) -> {
                            if (null != value && cause.wasEvicted()) {
                                value.completeExceptionally(new TimeoutException());
                            }
                        })
                .build();
    }

    public CompletableFuture<T> put(String txId) {
        CompletableFuture<T> result = new CompletableFuture<>();
        messagesAwaitMap.put(txId, result);
        return result;
    }

    public Boolean complete(String txId, T message) {
        return Optional.ofNullable(messagesAwaitMap.asMap().remove(txId))
                .map(it -> it.complete(message))
                .orElse(false);
    }

}
