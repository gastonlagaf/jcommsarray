package com.gastonlagaf.handler.model;

import com.gastonlagaf.stun.model.Message;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Getter
@RequiredArgsConstructor
public class PendingMessage {

    private final CompletableFuture<Message> messageFuture;

    private final LocalDateTime expirationTime;

}
