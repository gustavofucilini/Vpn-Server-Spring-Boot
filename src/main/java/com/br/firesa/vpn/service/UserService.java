package com.br.firesa.vpn.service;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.br.firesa.vpn.entity.User;
import com.br.firesa.vpn.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class UserService implements UserDetailsService {
	
	@Autowired
	private UserRepository userRepository;
	
	public User insert(User user) {
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
