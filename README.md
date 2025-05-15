# Project [NAME UNKNOWN]

## About

[NAME UNKNOWN] is a stack of modules, aimed to be a framework for application's 
network communication via UDP transport protocol. It includes server solutions 
for connection establishment, like STUN and TURN server, and sort of SIP server for
discovered addresses exchange between peers. And also client module provided, which 
allows to connect peers either directly, or via TURN proxy server.

## Modules description

**NOTE**: This is a brief description for understanding of project structure. 
Details like installation and usage described inside each module separately.

### [bootstrap-tests](bootstrap-tests/README.md)

Sample applications, doing some tests of communication, using different techniques.

### [client](client/README.md)

Main client library, implementing multiple connectivity techniques, like:
 - Direct connection
 - Direct connection after ICE discovery
 - TURN communication

### [codec-api](codec-api/README.md)

Base API, describing packet encoding and decoding mechanism for generic and custom 
communication protocols.

### [connection-utility](connection-utility/README.md)

Standalone sample application, aimed to be a replacement for bootstrap-tests module. 
So every type of communication is embedded in one utility, which chooses connection 
type, based from defined configuration.

### [signaling-model](signaling-model/README.md)

Package of models, used for addresses exchange between peers for connection 
establishment.

### [signaling-server](signaling-server/README.md)

Server, powered by Spring, used for addresses exchange between peers for connection 
establishment, able to be integrated.

### [signaling-server-test](signaling-server-test/README.md)

Sample standalone Spring Boot application with integrated signaling-server, used 
for testing of ICE procedure.

### [turn-codec](turn-codec/README.md)

codec-api implementation for STUN/TURN protocol.

### [turn-integrity](turn-integrity/README.md)

Includes support of integrity check for TURN protocol.

### [turn-model](turn-model/README.md)

Package of models, used for STUN/TURN protocol.

### [turn-server](turn-server/README.md)

Server, providing STUN and TURN protocol support.

### [udp-core](udp-core/README.md)

Core library for udp communication, describing udp package exchange logic, and how 
generic and custom protocols should be implemented.

