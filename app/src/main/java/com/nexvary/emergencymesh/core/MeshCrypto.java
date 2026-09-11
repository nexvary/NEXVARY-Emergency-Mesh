package com.nexvary.emergencymesh.core;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public final class MeshCrypto {
    private static final int SALT_BYTES = 16;
    private static final int IV_BYTES = 12;
    private static final int TAG_BITS = 128;
    private static final int KEY_BITS = 256;
    private static final int ITERATIONS = 120_000;
    private final SecureRandom random = new SecureRandom();

    public String encrypt(String plainText, char[] pin, String scope) throws GeneralSecurityException {
        if (plainText == null) throw new IllegalArgumentException("plainText required");
        byte[] salt = new byte[SALT_BYTES];
        byte[] iv = new byte[IV_BYTES];
        random.nextBytes(salt);
        random.nextBytes(iv);
        SecretKey key = derive(pin, salt, scope);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
        byte[] aad = scopeBytes(scope);
        cipher.updateAAD(aad);
        byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        ByteBuffer out = ByteBuffer.allocate(1 + SALT_BYTES + IV_BYTES + encrypted.length);
        out.put((byte) 1).put(salt).put(iv).put(encrypted);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(out.array());
    }

    public String decrypt(String encoded, char[] pin, String scope) throws GeneralSecurityException {
        byte[] all;
        try {
            all = Base64.getUrlDecoder().decode(encoded);
        } catch (RuntimeException e) {
            throw new GeneralSecurityException("Invalid encrypted payload", e);
        }
        if (all.length < 1 + SALT_BYTES + IV_BYTES + 16) throw new GeneralSecurityException("Encrypted payload too short");
        ByteBuffer in = ByteBuffer.wrap(all);
        int version = in.get() & 0xff;
        if (version != 1) throw new GeneralSecurityException("Unsupported crypto version");
        byte[] salt = new byte[SALT_BYTES];
        byte[] iv = new byte[IV_BYTES];
        in.get(salt).get(iv);
        byte[] encrypted = new byte[in.remaining()];
        in.get(encrypted);
        SecretKey key = derive(pin, salt, scope);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
        cipher.updateAAD(scopeBytes(scope));
        return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
    }

    private SecretKey derive(char[] pin, byte[] salt, String scope) throws GeneralSecurityException {
        if (pin == null || pin.length < 6) throw new GeneralSecurityException("Mesh PIN must have at least 6 characters");
        byte[] scopeBytes = scopeBytes(scope);
        ByteBuffer mixed = ByteBuffer.allocate(salt.length + scopeBytes.length);
        mixed.put(salt).put(scopeBytes);
        PBEKeySpec spec = new PBEKeySpec(pin, mixed.array(), ITERATIONS, KEY_BITS);
        byte[] raw = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        spec.clearPassword();
        return new SecretKeySpec(raw, "AES");
    }

    private byte[] scopeBytes(String scope) {
        return (scope == null ? "mesh" : scope).getBytes(StandardCharsets.UTF_8);
    }
}
