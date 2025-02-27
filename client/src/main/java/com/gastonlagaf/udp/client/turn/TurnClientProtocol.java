package com.gastonlagaf.udp.client.turn;

import com.gastonlagaf.udp.client.UdpClient;
import com.gastonlagaf.udp.client.model.ClientProperties;
import com.gastonlagaf.udp.client.protocol.BaseClientProtocol;
import com.gastonlagaf.udp.client.turn.client.TurnClient;
import com.gastonlagaf.udp.client.turn.client.impl.TurnUdpClient;
import com.gastonlagaf.udp.codec.CommunicationCodec;
import com.gastonlagaf.udp.protocol.ClientProtocol;
import com.gastonlagaf.udp.protocol.model.UdpPacketHandlerResult;
import com.gastonlagaf.udp.turn.codec.impl.MessageCodec;
import com.gastonlagaf.udp.turn.model.*;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;
import java.util.Set;

public class TurnClientProtocol<T> extends BaseClientProtocol<Message> {

    private static final Integer REQUIRED_HOST_ADDRESS_COUNT = 1;

    private static final Integer WORKERS_COUNT = 1;

    private static final Set<MessageType> RESPONSE_MESSAGE_TYPES = Set.of(
            MessageType.BINDING_REQUEST,
            MessageType.CHANNEL_BIND,
            MessageType.CREATE_PERMISSION,
            MessageType.ALLOCATE,
            MessageType.REFRESH
    );

    private final CommunicationCodec<Message> codec = new MessageCodec();

    private final Map<Integer, InetSocketAddress> channelBindings;

    private final ClientProtocol<T> targetProtocol;

    public TurnClientProtocol(ClientProtocol<T> targetProtocol, ClientProperties clientProperties) {
        super(NatBehaviour.NO_NAT, clientProperties, WORKERS_COUNT);
        this.targetProtocol = targetProtocol;
        this.channelBindings = new HashMap<>();
    }

    @Override
    protected String getCorrelationId(Message message) {
        return HexFormat.of().formatHex(message.getHeader().getTransactionId());
    }

    @Override
    protected UdpClient<Message> createUdpClient(UdpClient<Message> udpClient) {
        return new TurnUdpClient(udpClient, clientProperties.getHostAddress(), channelBindings);
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
        T message = targetProtocol.deserialize(receiverAddress, actualSender, ByteBuffer.wrap(data));
        targetProtocol.handle(receiverAddress, actualSender, message);
        return null;
    }

    @Override
    public void start(InetSocketAddress... addresses) {
        if (!REQUIRED_HOST_ADDRESS_COUNT.equals(addresses.length)) {
            throw new IllegalArgumentException("Only one host address allowed");
        }
        super.start(addresses);
        ((TurnClient) client).start(clientProperties.getTurnAddress());
    }

    private InetSocketAddress extractSender(Message message) {
        return switch (message.getHeader().getType()) {
            case DATA -> message.getAttributes().<AddressAttribute>get(KnownAttributeName.XOR_PEER_ADDRESS).toInetSocketAddress();
            case INBOUND_CHANNEL_DATA -> {
                Integer channelNumber = message.getAttributes().<ChannelNumberAttribute>get(KnownAttributeName.CHANNEL_NUMBER).getValue();
                yield channelBindings.get(channelNumber);
            }
            default -> null;
        };
    }

}
