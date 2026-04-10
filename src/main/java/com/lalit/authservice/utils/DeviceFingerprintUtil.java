package com.lalit.authservice.utils;

import java.security.MessageDigest;
import java.util.HexFormat;

public class DeviceFingerprintUtil {

    /**
     * Creates a hash from IP + UserAgent to identify a device
     * Same device = same fingerprint
     */
    public static String generateFingerprint(String ipAddress, String userAgent) {
        try {
            String raw = (ipAddress + "|" + userAgent).toLowerCase().trim();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes());
            return HexFormat.of().formatHex(hash).substring(0, 32); // first 32 chars
        } catch (Exception e) {
            return ipAddress + userAgent; // fallback
        }
    }
}