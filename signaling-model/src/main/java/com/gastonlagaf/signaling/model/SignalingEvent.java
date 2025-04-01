package com.gastonlagaf.signaling.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = RegisterEvent.class, name = "REGISTER"),
        @JsonSubTypes.Type(value = DeregisterEvent.class, name = "DEREGISTER"),
        @JsonSubTypes.Type(value = InviteEvent.class, name = "INVITE"),
        @JsonSubTypes.Type(value = CancelEvent.class, name = "CANCEL"),
        @JsonSubTypes.Type(value = InviteAnsweredEvent.class, name = "INVITE_ANSWERED"),
        @JsonSubTypes.Type(value = AcknowledgedEvent.class, name = "ACKNOWLEDGED"),
        @JsonSubTypes.Type(value = ClosingEvent.class, name = "CLOSING"),
        @JsonSubTypes.Type(value = ClosedEvent.class, name = "CLOSED"),
        @JsonSubTypes.Type(value = CreateSessionEvent.class, name = "CREATE_SESSION"),
        @JsonSubTypes.Type(value = SessionCreatedEvent.class, name = "SESSION_CREATED")
})
public abstract class SignalingEvent {

    private EventType type;

}
