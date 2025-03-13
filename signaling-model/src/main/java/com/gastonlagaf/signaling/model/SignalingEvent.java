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
        @JsonSubTypes.Type(value = OkEvent.class, name = "OK"),
        @JsonSubTypes.Type(value = AcknowledgedEvent.class, name = "ACKNOWLEDGED"),
        @JsonSubTypes.Type(value = ClosingEvent.class, name = "CLOSING")
})
public abstract class SignalingEvent {

    private EventType type;

}
