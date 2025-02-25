package com.gastonlagaf.udp.client.stun.model;

import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
@RequiredArgsConstructor
public class MessageAttributes {

    public static final MessageAttributes EMPTY = new MessageAttributes(Map.of());

    private final Map<Integer, List<MessageAttribute>> attributes;

    public static MessageAttributes singleOnly(Map<Integer, MessageAttribute> attributes) {
        Map<Integer, List<MessageAttribute>> mappedAttributes = attributes.entrySet().stream()
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
        return new MessageAttributes(mappedAttributes);
    }

    public void put(MessageAttribute attribute) {
        attributes.compute(attribute.getType(), (key, value) -> {
            if (null != value) {
                value.add(attribute);
                return value;
            }
            List<MessageAttribute> attributeList = new ArrayList<>();
            attributeList.add(attribute);
            return attributeList;
        });
    }

    public <T extends MessageAttribute> T get(int type) {
        List<MessageAttribute> attributeList = attributes.get(type);
        if (null == attributeList) {
            return null;
        }
        return (T) attributeList.getFirst();
    }

    public <T extends MessageAttribute> T get(KnownAttributeName attributeName) {
        return this.get(attributeName.getCode());
    }

    public <T extends MessageAttribute> List<T> findAll(Integer type) {
        List<MessageAttribute> attributeList = attributes.get(type);
        if (null == attributeList) {
            return null;
        }
        return (List<T>) attributeList;
    }

    public <T extends MessageAttribute> List<T> findAll(KnownAttributeName attributeName) {
        return this.findAll(attributeName.getCode());
    }

    public int size() {
        return attributes.size();
    }

    public boolean isEmpty() {
        return attributes.isEmpty();
    }

    public boolean containsKey(Object key) {
        return attributes.containsKey(key);
    }

    public Set<Integer> keySet() {
        return attributes.keySet();
    }

    public Collection<MessageAttribute> values() {
        return attributes.values().stream()
                .flatMap(Collection::stream)
                .toList();
    }

}
