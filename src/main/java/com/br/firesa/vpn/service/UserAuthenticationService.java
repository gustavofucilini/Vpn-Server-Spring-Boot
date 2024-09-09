package com.br.firesa.vpn.service;

import java.security.KeyPair;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.br.firesa.vpn.dtos.LoginRequest;
import com.br.firesa.vpn.dtos.LoginResponse;
import com.br.firesa.vpn.entity.User;
import com.br.firesa.vpn.security.jwt.JwtTokenUtil;
import com.br.firesa.vpn.security.keygen.KeyGeneratorUtil;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class UserAuthenticationService {
	
	@Autowired
	private JwtTokenUtil tokenService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private AuthenticationManager authenticationManager;
	
	public LoginResponse login(LoginRequest userLoginRequest) throws Exception {
		authenticate(userLoginRequest.getUsername(), userLoginRequest.getPassword());
		User user = userService.findByUsername(userLoginRequest.getUsername());
		user = LoginRequest.LoginRequestToUser(userLoginRequest, user);
		byte[] userPublicKeyBytes = Base64.getDecoder().decode(user.getUserPublicKey());
		user.setUserPublicKey(userPublicKeyBytes);
		
		KeyPair serverKeyPair = KeyGeneratorUtil.generateKeyPair();
		
		user.setServerPrivateKey(KeyGeneratorUtil.getEncodedPrivateKey(serverKeyPair));
		user.setServerPublicKey(KeyGeneratorUtil.getEncodedPublicKey(serverKeyPair));
		
//		Set<Role> roles = user.getAuthorities().stream()
//				.map(grantedAuthority -> new Role(grantedAuthority.getAuthority())).collect(Collectors.toSet());
		
		String jwt = tokenService.generateToken(user);
		
		return new LoginResponse(user, jwt);
	}
	
	public User register(User user) {
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		return userService.insert(user);
	}
	
	private void authenticate(String username, String password) throws Exception {
	    try {
	        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
	    } catch (Exception e) {
	        throw new UsernameNotFoundException("Falha na autenticação para o usuário: " + username, e);
	    }
	}

}
