package com.jcommsarray.client.turn;

import com.jcommsarray.client.UdpClient;
import com.jcommsarray.client.model.ClientProperties;
import com.jcommsarray.client.protocol.TurnAwareClientProtocol;
import com.jcommsarray.client.turn.client.TurnClient;
import com.jcommsarray.client.turn.client.impl.TurnUdpClient;
import com.jcommsarray.codec.CommunicationCodec;
import com.jcommsarray.test.protocol.ClientProtocol;
import com.jcommsarray.test.protocol.model.UdpPacketHandlerResult;
import com.jcommsarray.test.socket.UdpSockets;
import com.jcommsarray.turn.codec.impl.MessageCodec;
import com.jcommsarray.turn.model.*;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;
import java.util.Set;

public class TurnClientProtocol<T> extends TurnAwareClientProtocol<Message> {

    private static final Set<MessageType> RESPONSE_MESSAGE_TYPES = Set.of(
            MessageType.BINDING_REQUEST,
            MessageType.CHANNEL_BIND,
            MessageType.CREATE_PERMISSION,
            MessageType.ALLOCATE,
            MessageType.REFRESH
    );

    private final CommunicationCodec<Message> codec = new MessageCodec();

    private final ClientProtocol<T> targetProtocol;

    private Map<Integer, InetSocketAddress> channelBindings;

    public TurnClientProtocol(TurnClientProtocol<?> baseTurnClientProtocol, ClientProtocol<T> targetProtocol) {
        super(baseTurnClientProtocol);
        this.targetProtocol = targetProtocol;
        this.channelBindings = baseTurnClientProtocol.channelBindings;
        this.selectionKey = baseTurnClientProtocol.selectionKey;
    }

    public TurnClientProtocol(UdpSockets udpSockets, ClientProtocol<T> targetProtocol, ClientProperties clientProperties) {
        super(NatBehaviour.NO_NAT, clientProperties, udpSockets);
        this.targetProtocol = targetProtocol;
    }

    @Override
    protected String getCorrelationId(Message message) {
        return HexFormat.of().formatHex(message.getHeader().getTransactionId());
    }

    @Override
    protected UdpClient<Message> createUdpClient(UdpClient<Message> udpClient) {
        this.channelBindings = new HashMap<>();
        return new TurnUdpClient(udpClient, clientProperties.getHostAddress(), this.channelBindings);
    }

    @Override
    public Message deserialize(InetSocketAddress receiverAddress, InetSocketAddress senderAddress, ByteBuffer buffer) {
        return codec.decode(buffer);
    }

    @Override
    public ByteBuffer serialize(Message packet) {
        return codec.encode(packet);
    }

    @Override
    public UdpPacketHandlerResult handle(InetSocketAddress receiverAddress, InetSocketAddress senderAddress, Message packet) {
        if (RESPONSE_MESSAGE_TYPES.contains(packet.getHeader().getType())) {
            String txId = HexFormat.of().formatHex(packet.getHeader().getTransactionId());
            pendingMessages.complete(txId, packet);
            return null;
        }
        InetSocketAddress actualSender = extractSender(packet);
        if (null == actualSender) {
            return null;
        }
        byte[] data = packet.getAttributes().<DefaultMessageAttribute>get(KnownAttributeName.DATA).getValue();
        targetProtocol.handle(receiverAddress, actualSender, ByteBuffer.wrap(data));
        return null;
    }

    @Override
    public void start() {
        super.start();
        ((TurnClient) this.getClient()).start(clientProperties.getTurnAddress());
    }

    private InetSocketAddress extractSender(Message message) {
        return switch (message.getHeader().getType()) {
            case DATA ->
                    message.getAttributes().<AddressAttribute>get(KnownAttributeName.XOR_PEER_ADDRESS).toInetSocketAddress();
            case OUTBOUND_CHANNEL_DATA -> {
                Integer channelNumber = message.getAttributes().<ChannelNumberAttribute>get(KnownAttributeName.CHANNEL_NUMBER).getValue();
                yield channelBindings.get(channelNumber);
            }
            default -> null;
        };
    }

}
