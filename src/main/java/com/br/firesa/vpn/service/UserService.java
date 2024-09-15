package com.br.firesa.vpn.service;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.br.firesa.vpn.entity.User;
import com.br.firesa.vpn.repository.UserRepository;
import com.br.firesa.vpn.validation.AoAlterar;
import com.br.firesa.vpn.validation.AoInserir;

import jakarta.transaction.Transactional;

@Service
public class UserService implements UserDetailsService {
	
	@Autowired
	private UserRepository userRepository;
	
	@Validated(AoInserir.class)
	public User insert(User user) {
		return userRepository.save(user);
	}

	@Validated(AoAlterar.class)
	public User update(User user) {
		return userRepository.save(user);
	}
	
	@Transactional
	public User findByUsername(String username) {
		return userRepository.findByUsername(username)
                .map(user -> {
                    Hibernate.initialize(user.getAuthorities());
                    return user;
                })
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));
		
	}

	@Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .map(user -> {
                    Hibernate.initialize(user.getAuthorities());
                    return user;
                })
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));
    }

}
