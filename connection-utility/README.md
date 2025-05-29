# Connection Utility

Test utility for checking connectivity, mostly during development. Comparing to
[bootstrap-tests](../bootstrap-tests/README.md) module, instead of having multiple
test classes, divided by scenarios and roles, there is only one class, where mode
and role is defined by configuration properties from environment variables. Below
listed set of arguments for each specific mode and role.

**NOTE**: For TURN, ICE and SIP connectivity test, refer to
[turn-server](../turn-server/README.md) and signaling-server-test modules
descriptions, how to bootstrap them, as they are required for testing.

## Modes

### Direct Receiver

- SOCKET_TIMEOUT - socket timeout (optional)

### Direct Initiator

- SOCKET_TIMEOUT - socket timeout (optional)
- TARGET_ADDRESS - address of target peer
- TARGET_PORT - port of target peer

### ICE Receiver

- HOST_ID - host identifier
- SIGNALING_SERVER - URI to signaling server
- STUN_SERVER - address to STUN server instance (port is 3478 by default)
- TURN_SERVER - address to TURN server instance (port is 3478 by default)
- SOCKET_TIMEOUT - socket timeout (optional)

### ICE Initiator

- HOST_ID - host identifier
- OPPONENT_ID - identifier of target peer
- SIGNALING_SERVER - URI to signaling server
- STUN_SERVER - address to STUN server instance (port is 3478 by default)
- TURN_SERVER - address to TURN server instance (port is 3478 by default)
- SOCKET_TIMEOUT - socket timeout (optional)