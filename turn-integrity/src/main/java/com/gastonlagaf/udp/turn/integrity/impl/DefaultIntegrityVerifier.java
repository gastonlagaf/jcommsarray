package com.gastonlagaf.udp.client.stun.integrity.impl;

import com.gastonlagaf.udp.client.stun.exception.StunProtocolException;
import com.gastonlagaf.udp.client.stun.integrity.IntegrityVerifier;
import com.gastonlagaf.udp.client.stun.integrity.model.IntegrityVerificationDetails;
import com.gastonlagaf.udp.client.stun.integrity.utils.IntegrityUtils;
import com.gastonlagaf.udp.client.stun.model.*;
import com.gastonlagaf.udp.client.stun.user.UserProvider;
import com.gastonlagaf.udp.client.stun.user.model.UserDetails;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

@RequiredArgsConstructor
public class DefaultIntegrityVerifier implements IntegrityVerifier {

    private final String realm;

    private final UserProvider userProvider;

    @Override
    public void check(Message message) {
        IntegrityVerificationDetails details = extractDetails(message);

        UserDetails userDetails = details.getUserDetails();
        byte[] key = IntegrityUtils.constructKey(
                details.getPasswordAlgorithm(), userDetails.getUsername(), realm, userDetails.getPassword()
        );

        byte[] reconstructedHash = IntegrityUtils.constructHash(
                details.getTargetAttribute().getPrecedingBytes(), key, details.getTargetAttribute().getIsSha256()
        );

        if (!Arrays.equals(details.getTargetAttribute().getValue(), reconstructedHash)) {
            throw new StunProtocolException("Message integrity mismatch", ErrorCode.UNAUTHENTICATED.getCode());
        }
    }

    private IntegrityVerificationDetails extractDetails(Message message) {
        MessageIntegrityAttribute targetAttribute = message.getAttributes().get(KnownAttributeName.MESSAGE_INTEGRITY_SHA256);
        if (null == targetAttribute) {
            throw new StunProtocolException("No auth details provided", ErrorCode.UNAUTHENTICATED.getCode());
        }

        DefaultMessageAttribute usernameAttribute = message.getAttributes().get(KnownAttributeName.USERNAME);
        if (null == usernameAttribute) {
            throw new StunProtocolException("Username not provided", ErrorCode.UNAUTHENTICATED.getCode());
        }
        String username = new String(usernameAttribute.getValue());
        UserDetails userDetails = userProvider.find(realm, username)
                .orElseThrow(() -> new StunProtocolException("User not found", ErrorCode.UNAUTHENTICATED.getCode()));

        PasswordAlgorithmAttribute passwordAlgorithmAttribute = message.getAttributes().get(KnownAttributeName.PASSWORD_ALGORITHM);
        PasswordAlgorithm passwordAlgorithm = Optional.ofNullable(passwordAlgorithmAttribute)
                .map(PasswordAlgorithmAttribute::getValue)
                .orElse(PasswordAlgorithm.MD5);

        return new IntegrityVerificationDetails(targetAttribute, userDetails, passwordAlgorithm);
    }

}
