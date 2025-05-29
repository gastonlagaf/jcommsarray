# UDP Core Module

Implements UDP communication, using NIO datagram channel from standard sdk.

## How it works

There are several core classes, creating the entire workflow

- **_UdpSockets_** - container of one or many working threads, handling IO operations in datagram sockets
- **_Protocol_** - encodes, decodes and handles incoming datagram packets. There is an extension of **_Protocol_**
  instance, called **_ClientProtocol_**, able not only handle, but also send packets
- **_UdpClient_** - sends outbound packets via corresponding socket

## UdpSockets

**_UdpSockets_** class contains worker threads. To be precise, it maps datagram sockets
to workers. Sockets are registered using class **_UdpChannelRegistry_**, where datagram
channel is created and linked to selector with minimum amount of active channels. Each
selector is handled by one worker thread. Each thread, during one cycle, selects all
available keys. Then for each key:

- Reads from channel, if there is something
- Writes to channel, if UdpClient put serialized entry to write queue.

### Socket Registration Details

Note, that when **_UdpChannelRegistry_** attaches socket to selector, resulting
**_SelectionKey_** contains attachment with several fields:

- Protocol, which handles incoming packets
- Write Queue, which is drained each cycle of worker thread

So removing this attachment will break socket handling logic, described above

## Protocol

It is a base interface, used for handling incoming udp packets. It contains
methods for encoding, decoding and handling. Handle method usually defines message
type and routes message to specific handler. Handler mappings mechanism is determined
by developer himself.

## ClientProtocol

It is a **_Protocol_** interface extension, which able to provide a client to send
outbound udp packets. Also, it supports request response semantics, via linking
request / response exchange via correlation identifier. Correlation identifier should
be defined in custom protocol by developer himself and also, **_ClientProtocol_**
implementation should contain implementation details, how correlation identifier
is extracted from protocol message.

### PendingMessages

**_PendingMessages_** is a utility class for applying request / response semantics,
containing pending completable futures. Each future is associated with correlation
identifier. Every future may complete or fail due to wait timeout. Note, that
completed futures with messages, containing application errors should be handled
in **_ClientProtocol_** implementation.

## UdpClient

**_UdpClient_** is producer of outbound udp packets. There is an abstraction, called
**_BaseUdpClient_**. It's send implementation just serializes object, then calls
**_send_** method from **_UdpSockets_** class. **_UdpSockets_** itself extracts
write queue, bound to socket and puts writing entry with target address, where
this packet should be sent.