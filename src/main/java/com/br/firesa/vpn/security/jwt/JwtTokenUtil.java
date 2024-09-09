package com.br.firesa.vpn.security.jwt;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.br.firesa.vpn.security.RSAKeyProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtTokenUtil {

    @Value("${jwt.token.validity}")
    private long jwtTokenValidity;

    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    public JwtTokenUtil(RSAKeyProperties rsaKeyProperties) {
        this.privateKey = rsaKeyProperties.getPrivateKey();
        this.publicKey = rsaKeyProperties.getPublicKey();
    }

    // Extract username from JWT token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extract expiration date from JWT token
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Extract specific claim from JWT token
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Retrieve all claims using the public key
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                   .setSigningKey(publicKey)
                   .build()
                   .parseClaimsJws(token)
                   .getBody();
    }

    // Check if the token has expired
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Generate token for user
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        // Extract roles from UserDetails and add to claims
        claims.put("roles", userDetails.getAuthorities()
                .stream()
                .map(authority -> authority.getAuthority())
                .collect(Collectors.toList()));
        return buildToken(claims, userDetails.getUsername(), jwtTokenValidity);
    }

    // Build the token with claims, subject, and expiration
    private String buildToken(Map<String, Object> claims, String subject, long validity) {
        return Jwts.builder()
                   .setClaims(claims)
                   .setSubject(subject)
                   .setIssuedAt(new Date())
                   .setExpiration(new Date(System.currentTimeMillis() + validity * 1000))
                   .signWith(privateKey, SignatureAlgorithm.RS256)
                   .compact();
    }

    // Validate token
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
