package com.br.firesa.vpn.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.br.firesa.vpn.controller.converter.MapConverter;
import com.br.firesa.vpn.dtos.LoginRequest;
import com.br.firesa.vpn.dtos.LoginResponse;
import com.br.firesa.vpn.entity.User;
import com.br.firesa.vpn.service.UserAuthenticationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class UserAuthenticationController {
	
	@Autowired
	private UserAuthenticationService userAuthenticationService;

	@Autowired
	private MapConverter mapConverter;

	// Login com validação e resposta adequada
	@PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) throws Exception {
		LoginResponse response = userAuthenticationService.login(loginRequest);
		return ResponseEntity.ok(mapConverter.toJsonMap(response)); // Retorna 200 OK
    }

	// Registro de usuário com validação e retorno do status correto
	@PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody User user) {
		User registeredUser = userAuthenticationService.register(user);
		return ResponseEntity.status(HttpStatus.CREATED).body(mapConverter.toJsonMap(registeredUser)); // Retorna 201 Created
	}
}