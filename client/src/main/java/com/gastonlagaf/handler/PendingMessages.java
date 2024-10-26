package com.gastonlagaf.handler;

import com.gastonlagaf.stun.model.Message;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalListener;
import com.github.benmanes.caffeine.cache.Scheduler;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

public class PendingMessages {

    private final Cache<String, CompletableFuture<Message>> messagesAwaitMap;

    public PendingMessages(Long timeoutMillis) {
        this.messagesAwaitMap = Caffeine.newBuilder()
                .scheduler(Scheduler.systemScheduler())
                .expireAfterWrite(Duration.ofMillis(timeoutMillis))
                .removalListener(
                        (RemovalListener<String, CompletableFuture<Message>>) (key, value, cause) -> {
                            if (null != value && cause.wasEvicted()) {
                                value.completeExceptionally(new TimeoutException());
                            }
                        })
                .build();
    }

    public CompletableFuture<Message> put(String txId) {
        CompletableFuture<Message> result = new CompletableFuture<>();
        messagesAwaitMap.put(txId, result);
        return result;
    }

    public void complete(String txId, Message message) {
        Optional.ofNullable(messagesAwaitMap.asMap().remove(txId)).ifPresent(it -> it.complete(message));
    }

}
