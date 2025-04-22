package com.gastonlagaf.udp.client.ice.candidate;

import com.gastonlagaf.udp.client.ice.exception.IceFailureException;
import com.gastonlagaf.udp.client.ice.model.*;
import com.gastonlagaf.udp.client.ice.protocol.IceProtocol;
import com.gastonlagaf.udp.client.model.ClientProperties;
import com.gastonlagaf.udp.client.stun.StunClientProtocol;
import com.gastonlagaf.udp.client.stun.client.StunClient;
import com.gastonlagaf.udp.client.stun.client.impl.UdpStunClient;
import com.gastonlagaf.udp.client.turn.TurnClientProtocol;
import com.gastonlagaf.udp.client.turn.proxy.TurnProxy;
import com.gastonlagaf.udp.protocol.ClientProtocol;
import com.gastonlagaf.udp.socket.UdpSockets;
import com.gastonlagaf.udp.turn.model.NatBehaviour;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.*;
import java.nio.channels.SelectionKey;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
public class CandidateSpotter {

    private final IceSession iceSession;

    private final IceProperties iceProperties;

    private final ClientProperties clientProperties;

    private final UdpSockets udpSockets;

    private final AtomicInteger portCounter;

    private final CompletableFuture<IceConnectResult> future;

    private final AtomicInteger localPreferenceCounter = new AtomicInteger(65000);

    public CandidateSpotter(UdpSockets udpSockets, IceSession iceSession, IceProperties iceProperties, ClientProperties clientProperties, CompletableFuture<IceConnectResult> future) {
        this.iceSession = iceSession;
        this.iceProperties = iceProperties;
        this.clientProperties = clientProperties;
        this.udpSockets = udpSockets;
        this.future = future;
        this.portCounter = new AtomicInteger(iceProperties.getMinPort());
    }

    public SortedSet<Candidate> search() {
        List<Candidate> hostCandidates = searchHostCandidates();

        TreeSet<Candidate> result = new TreeSet<>();
        result.addAll(hostCandidates);

        if (result.isEmpty()) {
            throw new IceFailureException("No candidates found");
        }

        Candidate localCandidate = result.first();

        List<Candidate> peerReflexiveCandidates = searchPeerReflexiveCandidates(localCandidate);
        result.addAll(peerReflexiveCandidates);

        List<Candidate> serverReflexiveCandidates = searchServerReflexiveCandidates(localCandidate);
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

    private List<Candidate> searchPeerReflexiveCandidates(Candidate localCandidate) {
        if (null == clientProperties.getStunAddress()) {
            return List.of();
        }
        Candidate candidate = bind(
                localCandidate.getHostAddress().getAddress(),
                CandidateType.PEER_REFLEXIVE,
                localPreferenceCounter.getAndDecrement()
        );
        return List.of(candidate);
    }

    private List<Candidate> searchServerReflexiveCandidates(Candidate localCandidate) {
        if (null == clientProperties.getTurnAddress()) {
            return List.of();
        }

        Candidate candidate = bind(
                localCandidate.getHostAddress().getAddress(),
                CandidateType.SERVER_REFLEXIVE,
                localPreferenceCounter.getAndDecrement()
        );
        return List.of(candidate);
    }

    private List<InetAddress> introspectNetworkInterface(NetworkInterface networkInterface) throws SocketException {
        if (!networkInterface.isUp() || networkInterface.isLoopback()) {
            return List.of();
        }
        return networkInterface.inetAddresses()
                .filter(it -> it instanceof Inet4Address && !it.isLinkLocalAddress())
                .collect(Collectors.toList());
    }

    private Candidate bind(InetAddress inetAddress, CandidateType type, Integer localPreference) {
        while (portCounter.get() < iceProperties.getMaxPort()) {
            try {
                return !CandidateType.PEER_REFLEXIVE.equals(type)
                        ? tryRegister(inetAddress, type, localPreference)
                        : tryRegisterPeerReflexive(inetAddress, localPreference);
            } catch (Exception ex) {
                log.error("", ex);
            }
        }
        throw new IceFailureException("Depleted component ports");
    }

    private Candidate tryRegisterPeerReflexive(InetAddress inetAddress, Integer localPreference) {
        int port = portCounter.getAndIncrement();
        ClientProperties localProperties = new ClientProperties(
                new InetSocketAddress(inetAddress, port),
                null,
                clientProperties.getStunAddress(),
                clientProperties.getTurnAddress(),
                clientProperties.getSocketTimeout()
        );
        InetSocketAddress actualAddress;
        try (StunClientProtocol stunClientProtocol = new StunClientProtocol(udpSockets, localProperties)) {
            stunClientProtocol.start();
            actualAddress = ((StunClient) stunClientProtocol.getClient()).getReflexiveAddress();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

        IceProtocol iceProtocol = new IceProtocol(udpSockets, iceSession, localProperties, future);
        iceProtocol.start();

        return new Candidate(
                localProperties.getHostAddress(), actualAddress, CandidateType.PEER_REFLEXIVE, localPreference,
                iceProperties.getComponentId(), iceProtocol
        );
    }

    private Candidate tryRegister(InetAddress inetAddress, CandidateType type, Integer localPreference) {
        int port = portCounter.getAndIncrement();
        ClientProperties localProperties = new ClientProperties(
                new InetSocketAddress(inetAddress, port),
                null,
                clientProperties.getStunAddress(),
                clientProperties.getTurnAddress(),
                clientProperties.getSocketTimeout()
        );
        NatBehaviour natBehaviour = CandidateType.SERVER_REFLEXIVE.equals(type)
                ? NatBehaviour.ADDRESS_AND_PORT_DEPENDENT
                : NatBehaviour.NO_NAT;

        IceProtocol iceProtocol = new IceProtocol(natBehaviour, iceSession, udpSockets, localProperties, future);
        iceProtocol.start();

        InetSocketAddress actualAddress = CandidateType.SERVER_REFLEXIVE.equals(type)
                ? ((TurnProxy<?>) iceProtocol.getClient()).getProxyAddress()
                : localProperties.getHostAddress();

        return new Candidate(
                localProperties.getHostAddress(), actualAddress, type, localPreference,
                iceProperties.getComponentId(), iceProtocol
        );
    }

}
