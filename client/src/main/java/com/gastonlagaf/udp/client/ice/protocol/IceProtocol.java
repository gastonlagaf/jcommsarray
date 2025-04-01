package com.gastonlagaf.udp.client.ice.protocol;

import com.gastonlagaf.udp.client.UdpClient;
import com.gastonlagaf.udp.client.ice.candidate.CandidateSpotter;
import com.gastonlagaf.udp.client.ice.model.Candidate;
import com.gastonlagaf.udp.client.ice.model.CandidateType;
import com.gastonlagaf.udp.client.ice.model.IceProperties;
import com.gastonlagaf.udp.client.ice.model.IceRole;
import com.gastonlagaf.udp.client.ice.transfer.CandidateTransferOperator;
import com.gastonlagaf.udp.client.model.ClientProperties;
import com.gastonlagaf.udp.client.protocol.BaseClientProtocol;
import com.gastonlagaf.udp.protocol.model.UdpPacketHandlerResult;
import com.gastonlagaf.udp.socket.UdpSockets;
import com.gastonlagaf.udp.turn.codec.impl.MessageCodec;
import com.gastonlagaf.udp.turn.model.Message;
import com.gastonlagaf.udp.turn.model.NatBehaviour;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

public class IceProtocol extends BaseClientProtocol<Message> {

    private final MessageCodec codec = new MessageCodec();

    private final CandidateSpotter candidateSpotter;

    private final CandidateTransferOperator candidateTransferOperator;

    private final List<Candidate> opponentCandidates = new ArrayList<>();

    public IceProtocol(UdpSockets udpSockets, ClientProperties clientProperties, CandidateSpotter candidateSpotter, CandidateTransferOperator candidateTransferOperator) {
        super(
                NatBehaviour.NO_NAT,
                clientProperties,
                udpSockets
        );
        this.candidateSpotter = candidateSpotter;
        this.candidateTransferOperator = candidateTransferOperator;
    }

    @Override
    protected String getCorrelationId(Message message) {
        return HexFormat.of().formatHex(message.getHeader().getTransactionId());
    }

    @Override
    protected UdpClient<Message> createUdpClient(UdpClient<Message> udpClient) {
        return udpClient;
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
        return null;
    }

    public DatagramChannel connect(IceProperties iceProperties) {
        List<Candidate> candidates = candidateSpotter.search();
        List<Candidate> targetCandidates = candidateTransferOperator.exchange(
                iceProperties.getSourceContactId(), iceProperties.getTargetContactId(), candidates
        );
        return null;
    }

}
