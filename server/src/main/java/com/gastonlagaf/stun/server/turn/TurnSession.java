package com.gastonlagaf.stun.server.turn;

import lombok.AccessLevel;
import lombok.Getter;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Getter
public class TurnSession {

    private final InetSocketAddress clientAddress;

    private final InetSocketAddress serverAddress;

    private final SelectionKey selectionKey;

    private final Set<InetSocketAddress> peers = new HashSet<>();

    @Getter(AccessLevel.NONE)
    private final Map<Integer, InetSocketAddress> addressesByChannel = new HashMap<>();

    @Getter(AccessLevel.NONE)
    private final Map<InetSocketAddress, Integer> channelsByAddress = new HashMap<>();

    public TurnSession(InetSocketAddress clientAddress, InetSocketAddress serverAddress, SelectionKey selectionKey) {
        this.clientAddress = clientAddress;
        this.serverAddress = serverAddress;
        this.selectionKey = selectionKey;
    }

    public void putChannel(Integer channel, InetSocketAddress address) {
        addressesByChannel.put(channel, address);
        channelsByAddress.put(address, channel);
        peers.add(address);
    }

    public void removeChannel(Integer channel) {
        InetSocketAddress address = addressesByChannel.remove(channel);
        if (null != address) {
            channelsByAddress.remove(address);
            peers.remove(address);
        }
    }

    public Boolean containsChannel(Integer channel) {
        return addressesByChannel.containsKey(channel);
    }

    public InetSocketAddress getAddressByChannel(Integer channel) {
        return addressesByChannel.get(channel);
    }

    public Integer getChannelByAddress(InetSocketAddress address) {
        return channelsByAddress.get(address);
    }

}
