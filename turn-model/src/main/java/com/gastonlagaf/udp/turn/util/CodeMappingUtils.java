package com.gastonlagaf.udp.client.stun.util;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CodeMappingUtils {

    public static <E extends Enum<?>> Map<Integer, E> mapValues(E[] enumeration, Function<E, Integer> extractor) {
        return Arrays.stream(enumeration).collect(Collectors.toMap(extractor, Function.identity()));
    }

}
