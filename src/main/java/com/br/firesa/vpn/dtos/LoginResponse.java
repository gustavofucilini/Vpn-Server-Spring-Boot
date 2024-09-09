package com.br.firesa.vpn.dtos;

import java.util.Base64;

import com.br.firesa.vpn.entity.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
	
	private Long id;
	private String username;
    private String jwt;
    private String serverPublicKey;
    
	public LoginResponse(User user, String jwt) {
		this.id = user.getId();
		this.username = user.getUsername();
		this.jwt = jwt;
		this.serverPublicKey = Base64.getEncoder().encodeToString(user.getServerPublicKey());
	}
    
}
