package com.br.firesa.vpn.dtos;

import com.br.firesa.vpn.entity.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {

    private String username;
    private String password;
    private String userPublicKey;
	
    public static User LoginRequestToUser(LoginRequest loginRequest, User user) {
    	user.setUsername(loginRequest.username);
    	user.setUserPublicKey(loginRequest.getUserPublicKey().getBytes());
		return user;
	}
	
}
