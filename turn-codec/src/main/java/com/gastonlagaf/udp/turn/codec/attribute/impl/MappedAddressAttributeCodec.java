package com.gastonlagaf.udp.client.stun.codec.attribute.impl;

import com.gastonlagaf.udp.client.stun.codec.attribute.BaseMessageAttributeCodec;
import com.gastonlagaf.udp.client.stun.codec.util.CodecUtils;
import com.gastonlagaf.udp.client.stun.model.AddressAttribute;
import com.gastonlagaf.udp.client.stun.model.IpFamily;
import com.gastonlagaf.udp.client.stun.model.KnownAttributeName;
import com.gastonlagaf.udp.client.stun.model.MessageHeader;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class MappedAddressAttributeCodec extends BaseMessageAttributeCodec<AddressAttribute> {

    private static final Integer ATTRIBUTE_DETAILS_LENGTH = 4;

    private static final Integer PORT_XOR_MASK_SHIFT = 80;

    @Override
    protected Class<AddressAttribute> getType() {
        return AddressAttribute.class;
    }

    @Override
    protected byte[] encodeValue(MessageHeader messageHeader, AddressAttribute messageAttribute) {
        int portXorMask = messageAttribute.getXored() ? messageHeader.getMagicCookie() >> PORT_XOR_MASK_SHIFT : 0;
        int port = messageAttribute.getPort() ^ portXorMask;

        byte[] addressXorMask = getAddressXorMask(messageAttribute.getXored(), messageHeader, messageAttribute.getIpFamily());
        byte[] address = messageAttribute.toInetAddress().getAddress();
        for (int i = 0; i < messageAttribute.getIpFamily().getAddressLength(); i++) {
            address[i] = (byte) (address[i] ^ addressXorMask[i]);
        }

        ByteBuffer buffer = ByteBuffer.allocate(ATTRIBUTE_DETAILS_LENGTH + messageAttribute.getIpFamily().getAddressLength());
        buffer.put(CodecUtils.shortToByteArray(messageAttribute.getIpFamily().getCode().shortValue()));
        buffer.put(CodecUtils.shortToByteArray((short) port));
        buffer.put(address);

        return buffer.array();
    }

    @Override
    protected AddressAttribute decodeValue(MessageHeader messageHeader, ByteBuffer byteBuffer, Integer type, Integer length) {
        Boolean xored = KnownAttributeName.XOR_MAPPED_ADDRESS.getCode().equals(type);

        int ipFamilyCode = CodecUtils.readShort(byteBuffer);
        IpFamily ipFamily = IpFamily.ofCode(ipFamilyCode);

        int portXorMask = xored ? messageHeader.getMagicCookie() >> PORT_XOR_MASK_SHIFT : 0;
        int port = CodecUtils.readShort(byteBuffer) ^ portXorMask;

        byte[] addressXorMask = getAddressXorMask(xored, messageHeader, ipFamily);
        byte[] address = new byte[ipFamily.getAddressLength()];
        byteBuffer.get(address);
        for (int i = 0; i < ipFamily.getAddressLength(); i++) {
            address[i] = (byte) (address[i] ^ addressXorMask[i]);
        }
        String addressStr;
        try {
            addressStr = InetAddress.getByAddress(address).getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        return new AddressAttribute(type, length, xored, ipFamily, port, addressStr);
    }

    private byte[] getAddressXorMask(Boolean xored, MessageHeader messageHeader, IpFamily ipFamily) {
        byte[] magicCookie = BigInteger.valueOf(messageHeader.getMagicCookie()).toByteArray();

        Integer length = ipFamily.getAddressLength();
        byte[] result = new byte[length];

        if (!xored) {
            return result;
        }
        System.arraycopy(magicCookie, 0, result, 0, magicCookie.length);
        if (IpFamily.IPV6.equals(ipFamily)) {
            byte[] transactionId = messageHeader.getTransactionId();
            System.arraycopy(transactionId, 0, result, magicCookie.length, transactionId.length);
        }
        return result;
    }

}
