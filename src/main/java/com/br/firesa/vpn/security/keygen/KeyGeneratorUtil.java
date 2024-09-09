package com.br.firesa.vpn.security.keygen;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.ECGenParameterSpec;

public class KeyGeneratorUtil {

    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
    	KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
    	keyPairGenerator.initialize(new ECGenParameterSpec("secp256r1"));
        return keyPairGenerator.generateKeyPair();
    }

    public static byte[] getEncodedPublicKey(KeyPair keyPair) {
        return keyPair.getPublic().getEncoded();
    }

    public static byte[] getEncodedPrivateKey(KeyPair keyPair) {
        return keyPair.getPrivate().getEncoded();
    }
}