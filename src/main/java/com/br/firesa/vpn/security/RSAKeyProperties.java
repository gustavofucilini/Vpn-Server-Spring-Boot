package com.br.firesa.vpn.security;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
public class RSAKeyProperties {

    private RSAPublicKey publicKey;
    private RSAPrivateKey privateKey;

    private final ResourceLoader resourceLoader;

    public RSAKeyProperties(
            @Value("${jwt.public.key}") String publicKeyPath,
            @Value("${jwt.private.key}") String privateKeyPath,
            ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
        try {
            this.publicKey = loadPublicKey(publicKeyPath);
            this.privateKey = loadPrivateKey(privateKeyPath);
        } catch (Exception e) {
            throw new IllegalStateException("Could not load RSA keys", e);
        }
    }

    private RSAPublicKey loadPublicKey(String path) throws Exception {
        String key = loadKeyContent(path)
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(key));
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return (RSAPublicKey) kf.generatePublic(keySpec);
    }

    private RSAPrivateKey loadPrivateKey(String path) throws Exception {
        String key = loadKeyContent(path)
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(key));
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey) kf.generatePrivate(keySpec);
    }

    private String loadKeyContent(String path) throws IOException {
        Resource resource = resourceLoader.getResource(path);
        try (InputStream inputStream = resource.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            return reader.lines()
                    .map(String::trim)
                    .filter(line -> !line.startsWith("-----"))
                    .collect(Collectors.joining());
        }
    }

    public RSAPublicKey getPublicKey() {
        return publicKey;
    }

    public RSAPrivateKey getPrivateKey() {
        return privateKey;
    }
}