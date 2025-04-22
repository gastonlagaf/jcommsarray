package com.gastonlagaf.udp.client.turn;

import com.gastonlagaf.udp.client.UdpClient;
import com.gastonlagaf.udp.client.model.ClientProperties;
import com.gastonlagaf.udp.client.protocol.BaseClientProtocol;
import com.gastonlagaf.udp.client.turn.client.TurnClient;
import com.gastonlagaf.udp.client.turn.client.impl.TurnUdpClient;
import com.gastonlagaf.udp.codec.CommunicationCodec;
import com.gastonlagaf.udp.protocol.ClientProtocol;
import com.gastonlagaf.udp.protocol.model.UdpPacketHandlerResult;
import com.gastonlagaf.udp.socket.UdpSockets;
import com.gastonlagaf.udp.turn.codec.impl.MessageCodec;
import com.gastonlagaf.udp.turn.model.*;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;
import java.util.Set;

public class TurnClientProtocol<T> extends BaseClientProtocol<Message> {

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
        return new TurnUdpClient(udpClient, clientProperties.getHostAddress(), clientProperties.getTurnAddress(), this.channelBindings);
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
        ((TurnClient)this.getClient()).start(clientProperties.getTurnAddress());
    }

    private InetSocketAddress extractSender(Message message) {
        return switch (message.getHeader().getType()) {
            case DATA -> message.getAttributes().<AddressAttribute>get(KnownAttributeName.XOR_PEER_ADDRESS).toInetSocketAddress();
            case OUTBOUND_CHANNEL_DATA -> {
                Integer channelNumber = message.getAttributes().<ChannelNumberAttribute>get(KnownAttributeName.CHANNEL_NUMBER).getValue();
                yield channelBindings.get(channelNumber);
            }
            default -> null;
        };
    }

}
