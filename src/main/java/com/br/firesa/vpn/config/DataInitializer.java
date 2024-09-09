package com.br.firesa.vpn.config;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.br.firesa.vpn.entity.Role;
import com.br.firesa.vpn.entity.User;
import com.br.firesa.vpn.repository.RoleRepository;
import com.br.firesa.vpn.repository.UserRepository;

@Configuration
public class DataInitializer {
	
	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncode;

	@Bean
	CommandLineRunner initRoles() {
		return args -> {
			for (ResourcePermission permission : ResourcePermission.values()) {
				createRoleIfNotFound(permission.getAuthority());
			}

			if (userRepository.findById(Long.valueOf("1")).isPresent()) {
				return;
			}

			Set<Role> roles = new HashSet<>();
			roles.add(roleRepository.findByAuthority("ADMIN").get());

			userRepository.save(new User("root", passwordEncode.encode("root"), roles));

		};
	}
	
	private void createRoleIfNotFound(String authority) {
		if (!roleRepository.existsByAuthority(authority)) {
			Role role = new Role();
			role.setAuthority(authority);
			roleRepository.save(role);
		}
	}

}
