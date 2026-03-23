package com.gmao.service;

import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.time.Instant;

/**
 * TOTP implementation per RFC 6238 (HMAC-SHA1, 6-digit, 30-second window).
 * No external library required.
 */
@Service
public class TwoFactorService {

    private static final String BASE32_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final int SECRET_SIZE = 20; // 160 bits
    private static final int TIME_STEP = 30;   // 30 seconds
    private static final int DIGITS = 6;
    private static final int WINDOW = 1;       // ±1 time step tolerance

    /**
     * Generate a random 160-bit Base32-encoded TOTP secret.
     */
    public String generateSecret() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[SECRET_SIZE];
        random.nextBytes(bytes);
        return base32Encode(bytes);
    }

    /**
     * Build the otpauth:// URI for QR code generation.
     */
    public String buildQrUri(String email, String secret) {
        String issuer = "GMAO%20System";
        return "otpauth://totp/" + issuer + ":" + encode(email)
                + "?secret=" + secret
                + "&issuer=" + issuer
                + "&algorithm=SHA1&digits=6&period=30";
    }

    /**
     * Verify a TOTP code against the secret.
     * Checks current time step and ±WINDOW steps for clock drift.
     */
    public boolean verifyCode(String secret, String code) {
        if (secret == null || code == null || code.length() != DIGITS) return false;
        try {
            long trimmed = Long.parseLong(code.trim());
            long currentStep = Instant.now().getEpochSecond() / TIME_STEP;
            byte[] secretBytes = base32Decode(secret);
            for (int i = -WINDOW; i <= WINDOW; i++) {
                long expected = generateCode(secretBytes, currentStep + i);
                if (expected == trimmed) return true;
            }
        } catch (NumberFormatException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    // --- Internal helpers ---

    private long generateCode(byte[] secret, long timeStep) {
        try {
            byte[] msg = longToBytes(timeStep);
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(secret, "HmacSHA1"));
            byte[] hash = mac.doFinal(msg);
            int offset = hash[hash.length - 1] & 0x0F;
            long trunc = ((hash[offset] & 0x7F) << 24)
                    | ((hash[offset + 1] & 0xFF) << 16)
                    | ((hash[offset + 2] & 0xFF) << 8)
                    | (hash[offset + 3] & 0xFF);
            int mod = 1;
            for (int i = 0; i < DIGITS; i++) mod *= 10;
            return trunc % mod;
        } catch (Exception e) {
            throw new RuntimeException("TOTP generation error", e);
        }
    }

    private byte[] longToBytes(long value) {
        byte[] result = new byte[8];
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte) (value & 0xFF);
            value >>= 8;
        }
        return result;
    }

    private String base32Encode(byte[] data) {
        StringBuilder sb = new StringBuilder();
        int buffer = 0, bitsLeft = 0;
        for (byte b : data) {
            buffer = (buffer << 8) | (b & 0xFF);
            bitsLeft += 8;
            while (bitsLeft >= 5) {
                bitsLeft -= 5;
                sb.append(BASE32_CHARS.charAt((buffer >> bitsLeft) & 0x1F));
            }
        }
        if (bitsLeft > 0) {
            buffer <<= (5 - bitsLeft);
            sb.append(BASE32_CHARS.charAt(buffer & 0x1F));
        }
        return sb.toString();
    }

    private byte[] base32Decode(String input) {
        input = input.toUpperCase().replaceAll("[^A-Z2-7]", "");
        int outputLen = input.length() * 5 / 8;
        byte[] output = new byte[outputLen];
        int buffer = 0, bitsLeft = 0, idx = 0;
        for (char c : input.toCharArray()) {
            int val = BASE32_CHARS.indexOf(c);
            if (val < 0) continue;
            buffer = (buffer << 5) | val;
            bitsLeft += 5;
            if (bitsLeft >= 8) {
                bitsLeft -= 8;
                output[idx++] = (byte) ((buffer >> bitsLeft) & 0xFF);
            }
        }
        return output;
    }

    private String encode(String s) {
        try {
            return java.net.URLEncoder.encode(s, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            return s;
        }
    }
}
