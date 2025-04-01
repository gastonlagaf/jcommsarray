package com.gastonlagaf.udp.client.ice.candidate;

import com.gastonlagaf.udp.client.ice.exception.IceFailureException;
import com.gastonlagaf.udp.client.ice.model.Candidate;
import com.gastonlagaf.udp.client.ice.model.CandidateType;
import com.gastonlagaf.udp.client.ice.model.IceProperties;
import com.gastonlagaf.udp.client.ice.protocol.IceProtocol;
import com.gastonlagaf.udp.client.model.ClientProperties;
import com.gastonlagaf.udp.client.stun.StunClientProtocol;
import com.gastonlagaf.udp.client.stun.client.impl.UdpStunClient;
import com.gastonlagaf.udp.client.turn.proxy.TurnProxy;
import com.gastonlagaf.udp.turn.model.Message;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class CandidateSpotter {

    private final IceProperties iceProperties;

    private final ClientProperties clientProperties;

    private final AtomicInteger portCounter;

    private final AtomicInteger localPreferenceCounter = new AtomicInteger(65000);

    public CandidateSpotter(IceProperties iceProperties, ClientProperties clientProperties) {
        this.iceProperties = iceProperties;
        this.clientProperties = clientProperties;
        this.portCounter = new AtomicInteger(iceProperties.getMinPort());
    }

    public List<Candidate> search() {
        List<Candidate> hostCandidates = searchHostCandidates();

        List<Candidate> result = new ArrayList<>(hostCandidates);

        if (result.isEmpty()) {
            throw new IceFailureException("No candidates found");
        }

        List<Candidate> peerReflexiveCandidates = searchPeerReflexiveCandidates(result.getFirst().getProtocol());
        result.addAll(peerReflexiveCandidates);

        List<Candidate> serverReflexiveCandidates = searchServerReflexiveCandidates();
        result.addAll(serverReflexiveCandidates);

        return result;
    }

    private List<Candidate> searchHostCandidates() {
        List<InetAddress> bases = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                List<InetAddress> addresses = introspectNetworkInterface(networkInterface);
                bases.addAll(addresses);
            }
        } catch (SocketException e) {
            throw new UncheckedIOException(e);
        }

        return bases.stream()
                .map(it -> bind(it, CandidateType.HOST, localPreferenceCounter.getAndDecrement()))
                .toList();
    }

    private List<Candidate> searchPeerReflexiveCandidates(IceProtocol protocol) {
        if (null == clientProperties.getStunAddress()) {
            return List.of();
        }
        try (StunClientProtocol stunClientProtocol = new StunClientProtocol(null, clientProperties)) {
            InetSocketAddress socketAddress = ((UdpStunClient) stunClientProtocol.getClient()).getReflexiveAddress();
            Candidate candidate = new Candidate(
                    socketAddress, CandidateType.PEER_REFLEXIVE, localPreferenceCounter.getAndDecrement(),
                    iceProperties.getComponentId(), protocol
            );
            return List.of(candidate);
        } catch (IOException ex) {
            return List.of();
        }
    }

    private List<Candidate> searchServerReflexiveCandidates() {
        if (null == clientProperties.getTurnAddress()) {
            return List.of();
        }
        IceProtocol protocol = null;
        InetSocketAddress proxyAddress = ((TurnProxy<Message>)protocol.getClient()).getProxyAddress();
        Candidate candidate = new Candidate(
                proxyAddress, CandidateType.SERVER_REFLEXIVE, localPreferenceCounter.getAndDecrement(),
                iceProperties.getComponentId(), protocol
        );
        return List.of(candidate);
    }

    private List<InetAddress> introspectNetworkInterface(NetworkInterface networkInterface) throws SocketException {
        if (!networkInterface.isUp() || networkInterface.isLoopback()) {
            return List.of();
        }
        return networkInterface.inetAddresses()
                .filter(it -> it instanceof Inet4Address && it.isLinkLocalAddress())
                .collect(Collectors.toList());
    }

    private Candidate bind(InetAddress inetAddress, CandidateType type, Integer localPreference) {
        while (portCounter.get() < iceProperties.getMaxPort()) {
            Integer port = portCounter.getAndIncrement();
            ClientProperties localProperties = new ClientProperties(
                    new InetSocketAddress(inetAddress, port),
                    null,
                    clientProperties.getStunAddress(),
                    clientProperties.getTurnAddress(),
                    clientProperties.getSocketTimeout()
            );
            IceProtocol protocol = null;
            try {
//                protocol.start(localProperties.getHostAddress());
            } catch (Exception ex) {
                continue;
            }
            return new Candidate(
                    clientProperties.getHostAddress(), type, localPreference,
                    iceProperties.getComponentId(), protocol
            );
        }
        throw new IceFailureException("Depleted component ports");
    }

}
