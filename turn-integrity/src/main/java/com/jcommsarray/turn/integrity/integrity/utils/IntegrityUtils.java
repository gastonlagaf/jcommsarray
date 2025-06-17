package com.jcommsarray.turn.integrity.integrity.utils;

import com.jcommsarray.turn.model.MessageHeader;
import com.jcommsarray.turn.model.PasswordAlgorithm;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
        byte[] lengthBytes = new byte[]{
                (byte) (lengthAsShort >> 8),
                (byte) (lengthAsShort)
        };
        System.arraycopy(lengthBytes, START_POSITION, bytes, 2, lengthBytes.length);
    }

    public static byte[] constructKey(PasswordAlgorithm passwordAlgorithm, String username, String realm, String password) {
        String rawKey = String.format(RAW_KEY_FORMAT, username, realm, password);
        System.out.println(rawKey);
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance(passwordAlgorithm.getDigestName());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return digest.digest(rawKey.getBytes());
    }

    public static byte[] constructHash(byte[] precedingBytes, byte[] key, boolean isSha256) {
        int hashLength = isSha256 ? HMAC_SHA256_LENGTH : HMAC_SHA1_LENGTH;
        int newLength = precedingBytes.length + hashLength + TYPE_AND_LENGTH_OFFSET - MessageHeader.LENGTH;
        IntegrityUtils.setLength(precedingBytes, newLength);
        String macAlgoName = isSha256 ? HMAC_SHA256 : HMAC_SHA1;
        try {
            Mac mac = Mac.getInstance(macAlgoName);
            mac.init(new SecretKeySpec(key, macAlgoName));
            return mac.doFinal(precedingBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

}
