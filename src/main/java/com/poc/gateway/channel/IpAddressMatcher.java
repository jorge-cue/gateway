package com.poc.gateway.channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class IpAddressMatcher {

    private static final Logger log = LoggerFactory.getLogger(IpAddressMatcher.class);
    private final int numMaskedBits;
    private final InetAddress requiredAddress;

    /**
     * Takes a specific IP address or a range specified as IP/NetMask (eg.
     * 192.168.1.0/24 or 202.24.0.0/14 )
     */
    public IpAddressMatcher(String ipAddress) {
        log.info("Building ipAddressMatcher for network spec {}", ipAddress);
        if (ipAddress.indexOf('/') > 0) {
            var addressAndMask = ipAddress.split("/");
            ipAddress = addressAndMask[0];
            numMaskedBits = Integer.parseInt(addressAndMask[1]);
        } else {
            numMaskedBits = -1;
        }
        requiredAddress = parseAddress(ipAddress);
        assert (requiredAddress.getAddress().length * 8 >= numMaskedBits): String.format("IP Address %s is too short for bitmask of length %d", ipAddress, numMaskedBits);
    }

    public boolean matches(String address) {
        log.info("Matching address {} against network {} using mask {}", address, requiredAddress, numMaskedBits);
        InetAddress remoteAddress = parseAddress(address);
        if (!requiredAddress.getClass().equals(remoteAddress.getClass())) {
            return false;
        }
        if (numMaskedBits < 0) {
            return remoteAddress.equals(requiredAddress);
        }
        final var remoteAddressBytes = remoteAddress.getAddress();
        final var requiredAddressBytes = requiredAddress.getAddress();
        final var nonMaskedBytes = remoteAddressBytes.length - numMaskedBits / 8 - (numMaskedBits % 8 != 0 ? 1 : 0);
        final var maskForFinalByte = (byte) (0xFF00 >>> (numMaskedBits & 0x07));
        for (int i = 0; i < nonMaskedBytes; i++) {
            if (remoteAddressBytes[i] != requiredAddressBytes[i])
                return false;
        }
        if (maskForFinalByte != 0) {
            return (remoteAddressBytes[nonMaskedBytes] & maskForFinalByte) == (requiredAddressBytes[nonMaskedBytes] & maskForFinalByte);
        }
        return true;
    }

    private InetAddress parseAddress(String ipAddress) {
        try {
            return InetAddress.getByName(ipAddress);
        } catch (UnknownHostException x) {
            throw new IllegalArgumentException("Failed to parse address " + ipAddress, x);
        }
    }
}
