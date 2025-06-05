package com.jcommsarray.signaling.autoconfigure;

import com.jcommsarray.signaling.properties.SignalingServerProperties;
import com.jcommsarray.signaling.repository.SessionDatastore;
import com.jcommsarray.signaling.repository.SubscriberDatastore;
import com.jcommsarray.signaling.repository.impl.InMemorySessionDatastore;
import com.jcommsarray.signaling.repository.impl.InMemorySubscriberDatastore;
import com.jcommsarray.signaling.service.SessionService;
import com.jcommsarray.signaling.service.SignalingSubscriberService;
import com.jcommsarray.signaling.service.impl.DefaultSessionService;
import com.jcommsarray.signaling.service.impl.DefaultSignalingSubscriberService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.Optional;

@Configuration
@EnableWebSocketMessageBroker
@EnableConfigurationProperties({SignalingServerProperties.class})
@RequiredArgsConstructor
public class SignalingServerAutoconfiguration implements WebSocketMessageBrokerConfigurer {

    private final SignalingServerProperties properties;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/events");
        registry.setApplicationDestinationPrefixes("/actions");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        String wsEndpoint = Optional.ofNullable(properties.getWsEndpoint()).orElse("/ws");
        String[] originPatterns = Optional.ofNullable(properties.getOriginPatterns()).orElse(new String[] { "*" });
        registry.addEndpoint(wsEndpoint)
                .setAllowedOriginPatterns(originPatterns);
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
