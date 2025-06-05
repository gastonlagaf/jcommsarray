package com.jcommsarray.signaling.properties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@Getter
@RequiredArgsConstructor(onConstructor = @__({@ConstructorBinding}))
@ConfigurationProperties(prefix = "jcommsarray.signalling")
public class SignalingServerProperties {

    private final String wsEndpoint;

    private final String[] originPatterns;

}
