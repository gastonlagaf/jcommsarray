package com.gastonlagaf.signaling.autoconfigure;

import com.gastonlagaf.signaling.repository.SessionDatastore;
import com.gastonlagaf.signaling.repository.SubscriberDatastore;
import com.gastonlagaf.signaling.repository.impl.InMemorySessionDatastore;
import com.gastonlagaf.signaling.repository.impl.InMemorySubscriberDatastore;
import com.gastonlagaf.signaling.service.SessionService;
import com.gastonlagaf.signaling.service.SignalingSubscriberService;
import com.gastonlagaf.signaling.service.impl.DefaultSessionService;
import com.gastonlagaf.signaling.service.impl.DefaultSignalingSubscriberService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.AbstractHandshakeHandler;

import java.security.Principal;
import java.util.Map;
import java.util.Optional;

@Configuration
@EnableWebSocketMessageBroker
public class SignalingServerAutoconfiguration implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/ws/events");
        registry.setApplicationDestinationPrefixes("/events");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .addInterceptors()
//                .setHandshakeHandler(new AbstractHandshakeHandler() {
//            @Override
//            protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
//                return () -> "op";
//            }
//        })
                .setAllowedOriginPatterns("*");
    }

    @Bean
    public SignalingSubscriberService signalingSubscriberService(ObjectProvider<SubscriberDatastore> subscriberDatastore, SimpMessageSendingOperations messagingTemplate) {
        SubscriberDatastore datastore = Optional.ofNullable(subscriberDatastore.getIfAvailable())
                .orElseGet(InMemorySubscriberDatastore::new);
        return new DefaultSignalingSubscriberService(datastore, messagingTemplate);
    }

    @Bean
    public SessionService sessionService(ObjectProvider<SessionDatastore> sessionDatastore, SignalingSubscriberService signalingSubscriberService) {
        SessionDatastore datastore = Optional.ofNullable(sessionDatastore.getIfAvailable())
                .orElseGet(InMemorySessionDatastore::new);
        return new DefaultSessionService(datastore, signalingSubscriberService);
    }

}
