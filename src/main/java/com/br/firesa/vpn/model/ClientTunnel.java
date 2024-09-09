package com.br.firesa.vpn.model;

import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.spec.SecretKeySpec;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ClientTunnel {
	
	private PublicKey publicKey;
	private PrivateKey privateKey;
	private SecretKeySpec aesKey;
	
    private String serverAddress;
    private int serverPort;
    
    public void initializeTunnel() {
    }

}
