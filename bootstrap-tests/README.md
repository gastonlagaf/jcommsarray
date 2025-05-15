# Bootstrap Tests

It is a sample applications, used for testing in development.

## Test scenarios

**NOTE**: For TURN, ICE and SIP connectivity test, refer to turn-server and 
signaling-server-test modules descriptions, how to bootstrap them, as they are 
required for testing.

### Direct Communication

- Launch **_PureClient_** as a connection receiver.
- Launch **_PureSenderClient_** as a connection initiator

As a result, **_PureSenderClient_** should send 100 Pings and receive same amount of responses.

### Turn Communication

- Launch STUN / TURN Server from turn-server module.
- Launch **_PureClient_** as a connection receiver.
- Launch **_TurnChannelClient_** as a connection initiator via TURN session.

As a result, **_TurnChannelClient_** should send 100 Pings and receive same amount of responses.

### ICE Communication

- Launch STUN / TURN Server from turn-server module.
- Launch Signaling Server from signaling-server-test module.
- Launch **_IceReceiver_** as a connection receiver.
- Launch **_IceInitiator_** as a connection initiator.
  
As a result, **_IceInitiator_** should send 60 Pings and receive same amount of responses.

### SIP Connectivity

- Launch Signaling Server from signaling-server-test module.
- Launch **_SignalingReceiverClient_** as a SIP Session receiver.
- Launch **_SignalingSenderClient_** as a SIP Session initiator.

As a result, **_SignalingSenderClient_** should obtain all contact information from **_SignalingReceiverClient_**.
