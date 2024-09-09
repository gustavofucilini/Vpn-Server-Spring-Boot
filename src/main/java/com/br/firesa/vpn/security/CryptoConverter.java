package com.br.firesa.vpn.security;

import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class CryptoConverter implements AttributeConverter<byte[], byte[]> {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128;
    private static final int IV_LENGTH_BYTE = 12;
    private final SecretKey key;

    public CryptoConverter() throws Exception {
        String encodedKey = "WmhTb2x1dGlvbjIwMjRAQA=="; // Substitua pela chave armazenada em Base64
        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
        
        // Garantir que a chave tenha 16, 24 ou 32 bytes
        if (decodedKey.length != 16 && decodedKey.length != 24 && decodedKey.length != 32) {
            throw new IllegalArgumentException("Invalid AES key length: " + decodedKey.length + " bytes");
        }
        
        this.key = new SecretKeySpec(decodedKey, "AES");
    }

    @Override
    public byte[] convertToDatabaseColumn(byte[] attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            byte[] iv = new byte[IV_LENGTH_BYTE];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);
            byte[] encryptedData = cipher.doFinal(attribute);
            byte[] encryptedIvAndData = new byte[IV_LENGTH_BYTE + encryptedData.length];
            System.arraycopy(iv, 0, encryptedIvAndData, 0, IV_LENGTH_BYTE);
            System.arraycopy(encryptedData, 0, encryptedIvAndData, IV_LENGTH_BYTE, encryptedData.length);
            return encryptedIvAndData;
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting data", e);
        }
    }

    @Override
    public byte[] convertToEntityAttribute(byte[] dbData) {
        if (dbData == null) {
            return null;
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            byte[] iv = new byte[IV_LENGTH_BYTE];
            System.arraycopy(dbData, 0, iv, 0, iv.length);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);
            return cipher.doFinal(dbData, IV_LENGTH_BYTE, dbData.length - IV_LENGTH_BYTE);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting data", e);
        }
    }
}