package com.gastonlagaf.stun.integrity.utils;

import com.gastonlagaf.stun.model.MessageHeader;
import com.gastonlagaf.stun.model.PasswordAlgorithm;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IntegrityUtils {

    private static final Integer HMAC_SHA1_LENGTH = 20;

    private static final Integer HMAC_SHA256_LENGTH = 32;

    private static final String HMAC_SHA1 = "HmacSHA1";

    private static final String HMAC_SHA256 = "HmacSHA256";

    private static final String RAW_KEY_FORMAT = "%s:%s:%s";

    private static final Integer TYPE_AND_LENGTH_OFFSET = 4;

    private static final Integer START_POSITION = 0;

    public static void setLength(byte[] bytes, int length) {
        short lengthAsShort = (short) length;
        byte[] lengthBytes = new byte[] {
                (byte) (lengthAsShort >> 8),
                (byte) (lengthAsShort)
        };
        System.arraycopy(lengthBytes, START_POSITION, bytes, 2, lengthBytes.length);
    }

    @SneakyThrows
    public static byte[] constructKey(PasswordAlgorithm passwordAlgorithm, String username, String realm, String password) {
        String rawKey = String.format(RAW_KEY_FORMAT, username, realm, password);
        MessageDigest digest = MessageDigest.getInstance(passwordAlgorithm.getDigestName());
        return digest.digest(rawKey.getBytes());
    }

    @SneakyThrows
    public static byte[] constructHash(byte[] precedingBytes, byte[] key, boolean isSha256) {
        int hashLength = isSha256 ? HMAC_SHA256_LENGTH : HMAC_SHA1_LENGTH;
        int newLength = precedingBytes.length + hashLength + TYPE_AND_LENGTH_OFFSET - MessageHeader.LENGTH;
        IntegrityUtils.setLength(precedingBytes, newLength);
        String macAlgoName = isSha256 ? HMAC_SHA256 : HMAC_SHA1;
        Mac mac = Mac.getInstance(macAlgoName);
        mac.init(new SecretKeySpec(key, macAlgoName));
        return mac.doFinal(precedingBytes);
    }

}
