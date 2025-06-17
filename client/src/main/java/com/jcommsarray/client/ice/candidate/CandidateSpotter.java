package com.jcommsarray.client.ice.candidate;

import com.jcommsarray.client.ice.exception.IceFailureException;
import com.jcommsarray.client.ice.model.Candidate;
import com.jcommsarray.client.ice.model.CandidateType;
import com.jcommsarray.client.ice.model.IceProperties;
import com.jcommsarray.client.ice.model.IceSession;
import com.jcommsarray.client.ice.protocol.IceProtocol;
import com.jcommsarray.client.model.ClientProperties;
import com.jcommsarray.client.model.ConnectResult;
import com.jcommsarray.client.stun.StunClientProtocol;
import com.jcommsarray.client.stun.client.StunClient;
import com.jcommsarray.client.turn.proxy.TurnProxy;
import com.jcommsarray.test.discovery.InternetDiscovery;
import com.jcommsarray.test.exception.SocketRegistrationException;
import com.jcommsarray.test.socket.UdpSockets;
import com.jcommsarray.turn.integrity.integrity.IntegrityVerifier;
import com.jcommsarray.turn.integrity.user.model.UserDetails;
import com.jcommsarray.turn.model.NatBehaviour;
import lombok.extern.slf4j.Slf4j;

import java.net.*;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
public class CandidateSpotter {

    private final IceSession iceSession;

    private final IceProperties iceProperties;

    private final ClientProperties clientProperties;

    private final UdpSockets udpSockets;

    private final AtomicInteger portCounter;

    private final CompletableFuture<ConnectResult<IceProtocol>> future;

    private final UserDetails userDetails;

    private final IntegrityVerifier integrityVerifier;

    private final AtomicInteger localPreferenceCounter = new AtomicInteger(65000);

    public CandidateSpotter(UdpSockets udpSockets, IceSession iceSession, IceProperties iceProperties, ClientProperties clientProperties, CompletableFuture<ConnectResult<IceProtocol>> future, UserDetails userDetails, IntegrityVerifier integrityVerifier) {
        this.iceSession = iceSession;
        this.iceProperties = iceProperties;
        this.clientProperties = clientProperties;
        this.udpSockets = udpSockets;
        this.future = future;
        this.userDetails = userDetails;
        this.integrityVerifier = integrityVerifier;
        this.portCounter = new AtomicInteger(iceProperties.getMinPort());
    }

    public SortedSet<Candidate> search() {
        List<Candidate> hostCandidates = searchHostCandidates();

        TreeSet<Candidate> result = new TreeSet<>(hostCandidates);

        if (result.isEmpty()) {
            throw new IceFailureException("No local candidates found");
        }

        List<Candidate> peerReflexiveCandidates = search(hostCandidates, CandidateType.PEER_REFLEXIVE);
        result.addAll(peerReflexiveCandidates);

        List<Candidate> serverReflexiveCandidates = search(hostCandidates, CandidateType.SERVER_REFLEXIVE);
        result.addAll(serverReflexiveCandidates);

        return result;
    }

    private List<Candidate> searchHostCandidates() {
        return InternetDiscovery.getAddresses().stream()
                .map(it -> bind(it, CandidateType.HOST, localPreferenceCounter.getAndDecrement()))
                .toList();
    }

    private List<Candidate> search(List<Candidate> localCandidates, CandidateType candidateType) {
        if (null == candidateType || CandidateType.HOST.equals(candidateType)) {
            throw new IllegalArgumentException("Only stun and turn candidates required");
        }
        InetSocketAddress targetAddress = CandidateType.SERVER_REFLEXIVE.equals(candidateType)
                ? clientProperties.getTurnAddress()
                : clientProperties.getStunAddress();
        if (null == targetAddress) {
            return List.of();
        }
        for (Candidate localCandidate : localCandidates) {
            Candidate candidate = bind(
                    localCandidate.getHostAddress().getAddress(),
                    candidateType,
                    localPreferenceCounter.getAndDecrement()
            );
            if (null != candidate) {
                return List.of(candidate);
            }
        }
        return List.of();
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
        } catch (Exception ex) {
            if (ex instanceof SocketRegistrationException srex) {
                throw srex;
            }
            return null;
        }

        IceProtocol iceProtocol = new IceProtocol(udpSockets, iceSession, localProperties, future, userDetails, integrityVerifier);
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

        IceProtocol iceProtocol = new IceProtocol(natBehaviour, iceSession, udpSockets, localProperties, future, userDetails, integrityVerifier);
        try {
            iceProtocol.start();
        } catch (Exception ex) {
            if (ex instanceof SocketRegistrationException srex) {
                throw srex;
            }
            return null;
        }

        InetSocketAddress actualAddress = CandidateType.SERVER_REFLEXIVE.equals(type)
                ? ((TurnProxy<?>) iceProtocol.getClient()).getProxyAddress()
                : localProperties.getHostAddress();

        return new Candidate(
                localProperties.getHostAddress(), actualAddress, type, localPreference,
                iceProperties.getComponentId(), iceProtocol
        );
    }

}
