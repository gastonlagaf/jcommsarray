package com.jcommsarray.signaling.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignalingSubscriber {

    private String id;

    private String password;

    private List<AddressCandidate> addresses;

}
